package com.arsdevstudio.memoflow.ui.screens.recall

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.arsdevstudio.memoflow.MemoApplication
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.data.local.entity.UserEntity
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import com.arsdevstudio.memoflow.utils.BillingPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class RecallViewModel(
    application: Application,
    private val repository: MemoRepository,
    private val billingPrefs: BillingPrefs
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<RecallUiState>(RecallUiState.Loading)
    val uiState: StateFlow<RecallUiState> = _uiState.asStateFlow()

    private val _remainingRefreshes = MutableStateFlow(0)
    val remainingRefreshes: StateFlow<Int> = _remainingRefreshes.asStateFlow()

    private var isPremium = false

    init {
        checkLimitAndLoad()
    }

    private fun checkLimitAndLoad() {
        viewModelScope.launch {
            isPremium = billingPrefs.isPremium.first()
            val maxRecalls = if (isPremium) 6 else 2
            
            val user = repository.userSettings.first() ?: UserEntity()
            val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            if (user.lastRecallDate < today) {
                // Novo dia: reseta tudo no banco
                repository.saveUserSettings(user.copy(lastRecallDate = System.currentTimeMillis(), recallCount = 0))
                _remainingRefreshes.value = maxRecalls
                loadRandomOldNote()
            } else if (user.recallCount < maxRecalls) {
                // Ainda tem créditos
                _remainingRefreshes.value = maxRecalls - user.recallCount
                loadRandomOldNote()
            } else {
                // Limite diário atingido
                _remainingRefreshes.value = 0
                _uiState.value = RecallUiState.LimitReached
            }
        }
    }

    fun loadRandomOldNote() {
        viewModelScope.launch {
            val maxRecalls = if (isPremium) 6 else 2
            val user = repository.userSettings.first() ?: UserEntity()
            
            if (user.recallCount >= maxRecalls) {
                _uiState.value = RecallUiState.LimitReached
                _remainingRefreshes.value = 0
                return@launch
            }

            val notes = repository.getRecallableNotes().first()
            if (notes.isEmpty()) {
                _uiState.value = RecallUiState.Empty
            } else {
                val newCount = user.recallCount + 1
                repository.saveUserSettings(user.copy(recallCount = newCount, lastRecallDate = System.currentTimeMillis()))
                
                _remainingRefreshes.value = maxRecalls - newCount

                val oldestPool = notes.take(20)
                val randomNote = oldestPool.random()
                _uiState.value = RecallUiState.Success(randomNote)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as MemoApplication
                return RecallViewModel(application, application.repository, application.billingPrefs) as T
            }
        }
    }
}

sealed class RecallUiState {
    object Loading : RecallUiState()
    data class Success(val note: NoteEntity) : RecallUiState()
    object Empty : RecallUiState()
    object LimitReached : RecallUiState()
}

