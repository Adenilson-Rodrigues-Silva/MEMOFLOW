package com.example.memoflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserEntity(
    @PrimaryKey
    val id: Int = 0, // Apenas um usuário/configuração
    val userName: String = "",
    val email: String? = null,
    val firebaseUid: String? = null,
    val profilePhotoUri: String? = null,
    val bio: String = "",
    val pin: String? = null, // PIN de 4 números
    val isBiometricEnabled: Boolean = false,
    
    // Controle de Relembrar (Recall)
    val lastRecallDate: Long = 0, 
    val recallCount: Int = 0,
    
    // Controle de Gratidão (Pote)
    val lastGratitudeRecallDate: Long = 0,
    val gratitudeRecallCount: Int = 0,
    
    val isGoogleLogged: Boolean = false
)
