package com.example.memoflow.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.data.local.entity.UserEntity
import com.example.memoflow.data.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _userSettings = MutableStateFlow(UserEntity())
    val userSettings: StateFlow<UserEntity> = _userSettings.asStateFlow()

    init {
        loadUserSettings()
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            repository.userSettings.collectLatest { settings ->
                settings?.let {
                    _userSettings.value = it
                }
            }
        }
    }

    fun updateUserName(name: String) {
        saveSettings(_userSettings.value.copy(userName = name))
    }

    fun updateUserBio(bio: String) {
        saveSettings(_userSettings.value.copy(bio = bio))
    }

    fun updateUserPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                // Criar cópia física da imagem para persistência eterna
                val fileName = "profile_photo_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                
                val internalUri = Uri.fromFile(file).toString()
                saveSettings(_userSettings.value.copy(profilePhotoUri = internalUri))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveSettings(settings: UserEntity) {
        viewModelScope.launch {
            repository.saveUserSettings(settings)
            _userSettings.value = settings
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.memoflow.MemoApplication
                return ProfileViewModel(application.repository) as T
            }
        }
    }
}
