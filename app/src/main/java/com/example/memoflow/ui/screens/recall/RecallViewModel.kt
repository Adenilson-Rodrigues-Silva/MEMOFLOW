package com.example.memoflow.ui.screens.recall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.local.entity.UserEntity
import com.example.memoflow.data.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class RecallViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RecallUiState>(RecallUiState.Loading)
    val uiState: StateFlow<RecallUiState> = _uiState.asStateFlow()

    private val _remainingRefreshes = MutableStateFlow(2)
    val remainingRefreshes: StateFlow<Int> = _remainingRefreshes.asStateFlow()

    init {
        checkLimitAndLoad()
    }

    private fun checkLimitAndLoad() {
        viewModelScope.launch {
            val user = repository.userSettings.first() ?: UserEntity()
            val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            val isNewDay = user.lastRecallDate < today
            
            if (isNewDay) {
                // Reinicia contador para o novo dia
                repository.saveUserSettings(user.copy(lastRecallDate = System.currentTimeMillis(), recallCount = 0))
                _remainingRefreshes.value = 2
                loadRandomOldNote()
            } else if (user.recallCount <= 2) {
                // Ainda tem refreshes (1 inicial + 2 extras)
                _remainingRefreshes.value = 2 - user.recallCount
                loadRandomOldNote()
            } else {
                // Limite atingido
                _remainingRefreshes.value = 0
                _uiState.value = RecallUiState.LimitReached
            }
        }
    }

    fun loadRandomOldNote() {
        viewModelScope.launch {
            val user = repository.userSettings.first() ?: UserEntity()
            
            // Se não for a primeira carga do dia, incrementa o contador
            if (_uiState.value is RecallUiState.Success) {
                if (user.recallCount >= 2) {
                    _uiState.value = RecallUiState.LimitReached
                    return@launch
                }
                val newUser = user.copy(recallCount = user.recallCount + 1)
                repository.saveUserSettings(newUser)
                _remainingRefreshes.value = 2 - newUser.recallCount
            }

            val notes = repository.getRecallableNotes().first()
            if (notes.isEmpty()) {
                _uiState.value = RecallUiState.Empty
            } else {
                // Pega as 20 mais antigas (para dar mais variedade que 10)
                // e escolhe uma aleatória entre elas
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
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.memoflow.MemoApplication
                return RecallViewModel(application.repository) as T
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
