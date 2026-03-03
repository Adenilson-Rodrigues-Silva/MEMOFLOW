package com.example.memoflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gratitudes")
data class GratitudeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val date: Long = System.currentTimeMillis(),
    val colorHex: String,
    val year: Int
)
