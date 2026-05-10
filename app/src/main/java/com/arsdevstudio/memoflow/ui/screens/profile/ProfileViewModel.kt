package com.arsdevstudio.memoflow.ui.screens.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.arsdevstudio.memoflow.MemoApplication
import com.arsdevstudio.memoflow.data.local.entity.UserEntity
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import com.arsdevstudio.memoflow.utils.BillingPrefs
import com.arsdevstudio.memoflow.utils.NotificationHelper
import com.arsdevstudio.memoflow.utils.NotificationPhrases
import com.arsdevstudio.memoflow.utils.NotificationPrefs
import com.arsdevstudio.memoflow.utils.NotificationScheduler
import com.arsdevstudio.memoflow.utils.NotificationSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

import com.arsdevstudio.memoflow.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.delay

class ProfileViewModel(
    application: Application,
    private val repository: MemoRepository,
    private val notificationPrefs: NotificationPrefs,
    private val scheduler: NotificationScheduler,
    private val billingPrefs: BillingPrefs
) : AndroidViewModel(application) {

    private val _isChangingLanguage = MutableStateFlow(false)
    val isChangingLanguage = _isChangingLanguage.asStateFlow()

    private val _currentLanguagePhrase = MutableStateFlow("")
    val currentLanguagePhrase = _currentLanguagePhrase.asStateFlow()

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            val phrases = getApplication<Application>().resources.getStringArray(R.array.language_switch_phrases)
            _currentLanguagePhrase.value = phrases.random()
            _isChangingLanguage.value = true
            
            delay(2500) // Tempo para a animação inicial do Dino
            
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)

            // Como o ViewModel sobrevive à recriação da Activity, precisamos resetar o estado
            // após um pequeno delay para que a nova Activity tenha tempo de carregar
            // mantendo o overlay visível durante a transição de sistema.
            delay(2000) 
            _isChangingLanguage.value = false
        }
    }

    private val _userSettings = MutableStateFlow<UserEntity?>(null)
    val userSettings: StateFlow<UserEntity?> = _userSettings.asStateFlow()

    private val _notificationSettings = MutableStateFlow<NotificationSettings?>(null)
    val notificationSettings: StateFlow<NotificationSettings?> = _notificationSettings.asStateFlow()

    val isPremium: StateFlow<Boolean> = repository.userSettings
        .map { it?.isPremium ?: false }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

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
            
            _notificationSettings.value?.let { current ->
                if (key == NotificationPrefs.ALL_ENABLED) {
                    if (value) {
                        rescheduleAllEnabled(current)
                    } else {
                        scheduler.cancelAll()
                    }
                } else {
                    if (current.allEnabled) {
                        handleIndividualToggle(key, value, current)
                    }
                }
            }
        }
    }

    private fun handleIndividualToggle(
        key: androidx.datastore.preferences.core.Preferences.Key<Boolean>,
        value: Boolean,
        current: NotificationSettings
    ) {
        when (key) {
            NotificationPrefs.DAILY_ENABLED -> {
                if (value) scheduler.scheduleDailyReminder(current.dailyHour, current.dailyMinute)
                else scheduler.cancelWork("daily_reminder")
            }
            NotificationPrefs.GRATITUDE_ENABLED -> {
                if (value) scheduler.scheduleGratitudeReminder()
                else scheduler.cancelWork("gratitude")
            }
            NotificationPrefs.CAPSULE_ENABLED -> {
                if (value) scheduler.scheduleTimeCapsuleReminder()
                else scheduler.cancelWork("capsule")
            }
            NotificationPrefs.INSIGHT_ENABLED -> {
                if (value) scheduler.scheduleWeeklyInsight()
                else scheduler.cancelWork("insight")
            }
            NotificationPrefs.ECHO_ENABLED -> {
                if (value) scheduler.scheduleLockedNotesReminder()
                else scheduler.cancelWork("locked_notes")
            }
        }
    }

    private fun rescheduleAllEnabled(settings: NotificationSettings) {
        if (settings.dailyEnabled) scheduler.scheduleDailyReminder(settings.dailyHour, settings.dailyMinute)
        if (settings.gratitudeEnabled) scheduler.scheduleGratitudeReminder()
        if (settings.capsuleEnabled) scheduler.scheduleTimeCapsuleReminder()
        if (settings.insightEnabled) scheduler.scheduleWeeklyInsight()
        if (settings.echoEnabled) scheduler.scheduleLockedNotesReminder()
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            notificationPrefs.updateSettings { 
                it[NotificationPrefs.DAILY_HOUR] = hour
                it[NotificationPrefs.DAILY_MINUTE] = minute
            }
            _notificationSettings.value?.let { 
                if (it.allEnabled && it.dailyEnabled) {
                    scheduler.scheduleDailyReminder(hour, minute)
                }
            }
        }
    }

    fun triggerTestNotification(context: Context) {
        viewModelScope.launch {
            val settings = notificationPrefs.notificationSettings.first()
            val helper = NotificationHelper(context)
            val phrase = NotificationPhrases.getRandomPhrase(NotificationPhrases.dailyReminder)
            
            helper.showNotification(
                channelId = NotificationHelper.CHANNEL_DAILY,
                title = "Teste de Fluxo 🚀",
                message = phrase,
                soundEnabled = settings.soundEnabled,
                vibrationEnabled = settings.vibrationEnabled
            )
        }
    }

    fun updateUserName(name: String) {
        _userSettings.value?.let { current ->
            saveSettings(current.copy(userName = name))
        }
    }

    fun updateUserBio(bio: String) {
        _userSettings.value?.let { current ->
            saveSettings(current.copy(bio = bio))
        }
    }

    fun updateUserPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val current = _userSettings.value ?: return@launch
                val fileName = "profile_photo_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                val internalUri = Uri.fromFile(file).toString()
                saveSettings(current.copy(profilePhotoUri = internalUri))
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
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as MemoApplication
                return ProfileViewModel(
                    application,
                    application.repository,
                    NotificationPrefs(application.applicationContext),
                    NotificationScheduler(application.applicationContext),
                    application.billingPrefs
                ) as T
            }
        }
    }
}

