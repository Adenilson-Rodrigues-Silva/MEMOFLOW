package com.arsdevstudio.memoflow.ui.screens.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.arsdevstudio.memoflow.data.local.entity.UserEntity
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SecurityViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _userSettings = MutableStateFlow(UserEntity())
    val userSettings: StateFlow<UserEntity> = _userSettings.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userSettings.collectLatest { settings ->
                settings?.let { _userSettings.value = it }
            }
        }
    }

    fun updateBiometric(enabled: Boolean) {
        saveSettings(_userSettings.value.copy(isBiometricEnabled = enabled))
    }

    // Método para ser chamado pela UI para validar biometria
    fun checkBiometricStatus(): Boolean {
        return _userSettings.value.isBiometricEnabled
    }

    fun updatePin(pin: String?) {
        saveSettings(_userSettings.value.copy(pin = pin))
    }

    fun removePin() {
        viewModelScope.launch {
            val user = _userSettings.value
            val userId = user.firebaseUid ?: ""
            if (userId.isNotEmpty()) {
                repository.unlockAllNotes(userId)
            }
            saveSettings(user.copy(pin = null))
        }
    }

    private fun saveSettings(settings: UserEntity) {
        viewModelScope.launch {
            repository.saveUserSettings(settings)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.arsdevstudio.memoflow.MemoApplication
                return SecurityViewModel(application.repository) as T
            }
        }
    }
}

