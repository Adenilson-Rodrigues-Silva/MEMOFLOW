package com.example.memoflow.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.data.local.entity.UserEntity
import com.example.memoflow.data.repository.MemoRepository
import com.example.memoflow.utils.NotificationPrefs
import com.example.memoflow.utils.NotificationScheduler
import com.example.memoflow.utils.NotificationSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(
    private val repository: MemoRepository,
    private val notificationPrefs: NotificationPrefs,
    private val scheduler: NotificationScheduler
) : ViewModel() {

    private val _userSettings = MutableStateFlow(UserEntity())
    val userSettings: StateFlow<UserEntity> = _userSettings.asStateFlow()

    private val _notificationSettings = MutableStateFlow<NotificationSettings?>(null)
    val notificationSettings: StateFlow<NotificationSettings?> = _notificationSettings.asStateFlow()

    init {
        loadUserSettings()
        loadNotificationSettings()
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            repository.userSettings.collectLatest { settings ->
                settings?.let { _userSettings.value = it }
            }
        }
    }

    private fun loadNotificationSettings() {
        viewModelScope.launch {
            notificationPrefs.notificationSettings.collectLatest { settings ->
                _notificationSettings.value = settings
            }
        }
    }

    fun updateNotificationToggle(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            notificationPrefs.updateSettings { it[key] = value }
            // Se mudou o mestre ou o diário, re-agenda
            _notificationSettings.value?.let { current ->
                if (value && (key == NotificationPrefs.ALL_ENABLED || key == NotificationPrefs.DAILY_ENABLED)) {
                    scheduler.scheduleDailyReminder(current.dailyHour, current.dailyMinute)
                } else if (!value && (key == NotificationPrefs.ALL_ENABLED)) {
                    scheduler.cancelAll()
                }
            }
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            notificationPrefs.updateSettings { 
                it[NotificationPrefs.DAILY_HOUR] = hour
                it[NotificationPrefs.DAILY_MINUTE] = minute
            }
            scheduler.scheduleDailyReminder(hour, minute)
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
                val fileName = "profile_photo_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                val internalUri = Uri.fromFile(file).toString()
                saveSettings(_userSettings.value.copy(profilePhotoUri = internalUri))
            } catch (e: Exception) { e.printStackTrace() }
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
                return ProfileViewModel(
                    application.repository,
                    NotificationPrefs(application.applicationContext),
                    NotificationScheduler(application.applicationContext)
                ) as T
            }
        }
    }
}
