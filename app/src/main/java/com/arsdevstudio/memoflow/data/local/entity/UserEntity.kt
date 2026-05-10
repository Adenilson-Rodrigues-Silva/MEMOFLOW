package com.arsdevstudio.memoflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserEntity(
    @PrimaryKey
    val id: Int = 0,
    val userName: String = "",
    val email: String? = null,
    val firebaseUid: String? = null,
    val profilePhotoUri: String? = null,
    val bio: String = "",
    val pin: String? = null,
    val isBiometricEnabled: Boolean = false,
    val lastRecallDate: Long = 0, 
    val recallCount: Int = 0,
    val lastGratitudeRecallDate: Long = 0,
    val gratitudeRecallCount: Int = 0,
    val isGoogleLogged: Boolean = false,
    val hasSeenWelcome: Boolean = false, // ✅ Para saber se já passou pela tela inicial
    val isPremium: Boolean = false // ✅ Status profissional por conta
)

