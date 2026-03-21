package com.example.memoflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.memoflow.data.local.dao.GratitudeDao
import com.example.memoflow.data.local.dao.NoteDao
import com.example.memoflow.data.local.dao.UserDao
import com.example.memoflow.data.local.entity.GratitudeEntity
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.local.entity.UserEntity

@Database(
    entities = [NoteEntity::class, UserEntity::class, GratitudeEntity::class],
    version = 9, // Incrementada para incluir latitude e longitude em NoteEntity
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun gratitudeDao(): GratitudeDao
}
