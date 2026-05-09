package com.arsdevstudio.memoflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "INFO", "CAPSULE", "DONATION"
    val targetId: String? = null, // ID da nota ou outro alvo
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
