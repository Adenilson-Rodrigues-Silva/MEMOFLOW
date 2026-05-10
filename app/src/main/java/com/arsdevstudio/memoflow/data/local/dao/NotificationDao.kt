package com.arsdevstudio.memoflow.data.local.dao

import androidx.room.*
import com.arsdevstudio.memoflow.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllNotifications(userId: String)
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND targetId = :targetId AND type = :type LIMIT 1")
    suspend fun getNotificationByTarget(userId: String, targetId: String, type: String): NotificationEntity?
}
