package com.example.memoflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserEntity(
    @PrimaryKey
    val id: Int = 0, // Apenas um usuário/configuração
    val userName: String = "",
    val profilePhotoUri: String? = null,
    val bio: String = "",
    val pin: String? = null, // PIN de 4 números
    val isBiometricEnabled: Boolean = false,
    val lastRecallDate: Long = 0, // Timestamp do último acesso ao Recall
    val recallCount: Int = 0 // Contador de refreshes no dia
)
