package com.arsdevstudio.memoflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.arsdevstudio.memoflow.data.local.entity.NotificationEntity
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import com.arsdevstudio.memoflow.utils.BillingPrefs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModel(
    private val repository: MemoRepository,
    private val billingPrefs: BillingPrefs
) : ViewModel() {

    private val _userId = MutableStateFlow("")

    val notifications: StateFlow<List<NotificationEntity>> = _userId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else repository.getNotifications(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = _userId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(0)
        else repository.getUnreadNotificationCount(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            repository.userSettings.collectLatest { user ->
                _userId.value = user?.firebaseUid ?: ""
                if (_userId.value.isNotEmpty()) {
                    triggerCheck()
                }
            }
        }
    }

    fun triggerCheck() {
        val userId = _userId.value
        if (userId.isEmpty()) return
        viewModelScope.launch {
            checkCapsuleNotifications(userId)
            checkStreakNotifications(userId)
        }
    }

    private fun isReady(unlockDate: Long): Boolean {
        val now = System.currentTimeMillis()
        if (now >= unlockDate) return true
        
        val unlockDay = java.time.Instant.ofEpochMilli(unlockDate)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val today = java.time.LocalDate.now()
        return !today.isBefore(unlockDay)
    }

    private suspend fun checkStreakNotifications(userId: String) {
        val lastNote = repository.getAllNotes(userId).first().maxByOrNull { it.date }
        val twoDaysMillis = 2 * 24 * 60 * 60 * 1000L
        
        if (lastNote != null && (System.currentTimeMillis() - lastNote.date) > twoDaysMillis) {
            val existing = repository.getNotificationByTarget(userId, "STREAK_REMINDER", "INFO")
            if (existing == null || (System.currentTimeMillis() - existing.timestamp) > twoDaysMillis) {
                repository.insertNotification(
                    NotificationEntity(
                        userId = userId,
                        title = "Mantenha o Fluxo 🌊",
                        message = com.arsdevstudio.memoflow.utils.NotificationPhrases.streakPhrases.random(),
                        type = "INFO",
                        targetId = "STREAK_REMINDER"
                    )
                )
            }
        }
    }

    private suspend fun checkCapsuleNotifications(userId: String) {
        val locale = java.util.Locale("pt", "BR")
        val dateFormat = java.text.SimpleDateFormat("dd/MM", locale)
        
        // 1. Notificações de Cápsulas (In-app)
        repository.getAllNotes(userId).first().forEach { note ->
            if (note.isTimeCapsule && note.unlockDate != null && isReady(note.unlockDate)) {
                val existing = repository.getNotificationByTarget(userId, note.id.toString(), "CAPSULE")
                if (existing == null) {
                    repository.insertNotification(
                        NotificationEntity(
                            userId = userId,
                            title = "Cápsula pronta! ❄️",
                            message = "Você tem uma memória de ${dateFormat.format(java.util.Date(note.date))} pronta para ser aberta.",
                            type = "CAPSULE",
                            targetId = note.id.toString()
                        )
                    )
                }
            }
        }
        
        // 2. Ciclo de Doações (Uma vez por semana para todos, incentivando o apoio ao dev)
        val lastDonationTimestamp = billingPrefs.lastDonationShown.first()
        val oneWeekMillis = 7 * 24 * 60 * 60 * 1000L
        
        if (System.currentTimeMillis() - lastDonationTimestamp > oneWeekMillis) {
            val phrases = com.arsdevstudio.memoflow.utils.NotificationPhrases.donationPhrases
            val phrase = phrases.random() 
            
            repository.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = "Apoie o Dev ✨",
                    message = phrase,
                    type = "DONATION",
                    targetId = "donation_screen"
                )
            )
            billingPrefs.updateLastDonationShown(System.currentTimeMillis())
        }
    }

    fun markAsRead(notification: NotificationEntity) {
        viewModelScope.launch {
            repository.updateNotification(notification.copy(isRead = true))
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            if (_userId.value.isNotEmpty()) {
                repository.markAllNotificationsAsRead(_userId.value)
            }
        }
    }

    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.arsdevstudio.memoflow.MemoApplication
                return NotificationViewModel(application.repository, application.billingPrefs) as T
            }
        }
    }
}
