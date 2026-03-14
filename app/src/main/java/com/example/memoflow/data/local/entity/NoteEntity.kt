package com.example.memoflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val contentHtml: String,
    val emoji: String,
    val humor: String,
    val date: Long,
    val images: List<String>,
    val audioPath: String?,
    val isLocked: Boolean = false,
    val isTimeCapsule: Boolean = false,
    val unlockDate: Long? = null,
    val fontFamilyName: String? = null // Mantido apenas para evitar erro de schema do Room
)
