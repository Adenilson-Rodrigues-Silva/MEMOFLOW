package com.arsdevstudio.memoflow.data.repository

import com.arsdevstudio.memoflow.data.local.dao.GratitudeDao
import com.arsdevstudio.memoflow.data.local.dao.NotificationDao
import com.arsdevstudio.memoflow.data.local.dao.NoteDao
import com.arsdevstudio.memoflow.data.local.dao.UserDao
import com.arsdevstudio.memoflow.data.local.entity.GratitudeEntity
import com.arsdevstudio.memoflow.data.local.entity.NotificationEntity
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class MemoRepository(
    private val noteDao: NoteDao,
    private val userDao: UserDao,
    private val gratitudeDao: GratitudeDao,
    private val notificationDao: NotificationDao
) {
    // Notifications
    fun getNotifications(userId: String): Flow<List<NotificationEntity>> = notificationDao.getNotifications(userId)
    fun getUnreadNotificationCount(userId: String): Flow<Int> = notificationDao.getUnreadCount(userId)
    suspend fun insertNotification(notification: NotificationEntity) = notificationDao.insertNotification(notification)
    suspend fun updateNotification(notification: NotificationEntity) = notificationDao.updateNotification(notification)
    suspend fun markAllNotificationsAsRead(userId: String) = notificationDao.markAllAsRead(userId)
    suspend fun deleteNotification(notification: NotificationEntity) = notificationDao.deleteNotification(notification)
    suspend fun deleteAllNotifications(userId: String) = notificationDao.deleteAllNotifications(userId)
    suspend fun getNotificationByTarget(userId: String, targetId: String, type: String) = notificationDao.getNotificationByTarget(userId, targetId, type)

    // Notes
    fun getAllNotes(userId: String): Flow<List<NoteEntity>> = noteDao.getAllNotes(userId)

    suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    suspend fun deleteAllNotes(userId: String) = noteDao.deleteAllNotes(userId)
    suspend fun getNoteById(id: Long) = noteDao.getNoteById(id)
    suspend fun unlockAllNotes(userId: String) = noteDao.unlockAllNotes(userId)

    fun getNotesByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<NoteEntity>> {
        return noteDao.getNotesInDateRange(userId, startDate, endDate)
    }

    fun getGratitudesByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<GratitudeEntity>> {
        return gratitudeDao.getGratitudesInDateRange(userId, startDate, endDate)
    }

    suspend fun getTotalGratitudeCountSync(userId: String): Int = gratitudeDao.getTotalCount(userId)

    fun getRecallableNotes(userId: String): Flow<List<NoteEntity>> = noteDao.getRecallableNotes(userId)

    fun getNotesWithLocation(userId: String): Flow<List<NoteEntity>> = noteDao.getNotesWithLocation(userId)

    fun getNotesWithLocationSince(userId: String, sinceDate: Long): Flow<List<NoteEntity>> = 
        noteDao.getNotesWithLocationSince(userId, sinceDate)

    fun getNotesByLocationAreaFiltered(userId: String, minLat: Double, maxLat: Double, minLon: Double, maxLon: Double, sinceDate: Long): Flow<List<NoteEntity>> {
        return noteDao.getNotesByLocationAreaFiltered(userId, minLat, maxLat, minLon, maxLon, sinceDate)
    }

    suspend fun getNoteCountForToday(userId: String): Int {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return noteDao.getCountForDay(userId, startOfDay, endOfDay)
    }

    suspend fun getTimeCapsuleCount(userId: String): Int = noteDao.getTimeCapsuleCount(userId)

    // User Settings
    val userSettings: Flow<UserEntity?> = userDao.getUserSettings()
    suspend fun saveUserSettings(user: UserEntity) = userDao.insertUser(user)
    suspend fun getUserPin() = userDao.getUserPin()
    suspend fun updatePremiumStatus(isPremium: Boolean) {
        userDao.getUserSettingsSync()?.let { user ->
            userDao.insertUser(user.copy(isPremium = isPremium))
        }
    }

    // Gratitude
    fun getAllGratitudes(userId: String): Flow<List<GratitudeEntity>> = gratitudeDao.getAllGratitudes(userId)
    
    fun getGratitudesByYear(userId: String, year: Int) = gratitudeDao.getGratitudesByYear(userId, year)

    suspend fun insertGratitude(gratitude: GratitudeEntity) = gratitudeDao.insertGratitude(gratitude)
    
    suspend fun deleteGratitude(gratitude: GratitudeEntity) = gratitudeDao.deleteGratitude(gratitude)
    suspend fun deleteAllGratitudes(userId: String) = gratitudeDao.deleteAllGratitudes(userId)

    suspend fun getGratitudeCountForToday(userId: String): Int {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return gratitudeDao.getCountForDay(userId, startOfDay, endOfDay)
    }
}
