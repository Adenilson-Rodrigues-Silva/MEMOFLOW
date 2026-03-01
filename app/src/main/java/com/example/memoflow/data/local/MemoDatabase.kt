package com.example.memoflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.memoflow.data.local.dao.GoalDao
import com.example.memoflow.data.local.dao.NoteDao
import com.example.memoflow.data.local.dao.UserDao
import com.example.memoflow.data.local.entity.GoalEntity
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.local.entity.UserEntity

@Database(
    entities = [NoteEntity::class, UserEntity::class, GoalEntity::class],
    version = 2, // Subi para 2 para refletir a nova tabela de Metas
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun goalDao(): GoalDao
}
