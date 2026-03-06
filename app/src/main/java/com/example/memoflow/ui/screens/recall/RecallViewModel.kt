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
            
            if (user.lastRecallDate < today) {
                // Novo dia: reseta tudo no banco
                repository.saveUserSettings(user.copy(lastRecallDate = System.currentTimeMillis(), recallCount = 0))
                loadRandomOldNote()
            } else if (user.recallCount < 3) {
                // Ainda tem créditos (total 3 por dia)
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
            val user = repository.userSettings.first() ?: UserEntity()
            
            if (user.recallCount >= 3) {
                _uiState.value = RecallUiState.LimitReached
                _remainingRefreshes.value = 0
                return@launch
            }

            val notes = repository.getRecallableNotes().first()
            if (notes.isEmpty()) {
                _uiState.value = RecallUiState.Empty
            } else {
                // Incrementa o contador no banco DEPOIS de verificar se existem notas
                val newCount = user.recallCount + 1
                repository.saveUserSettings(user.copy(recallCount = newCount, lastRecallDate = System.currentTimeMillis()))
                
                // UI: Refreshes restantes = Total(3) - Já usados(newCount)
                // Se newCount for 1 (primeira nota), restam 2 refreshes.
                _remainingRefreshes.value = 3 - newCount

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
