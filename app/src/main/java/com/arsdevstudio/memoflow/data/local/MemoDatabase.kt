package com.arsdevstudio.memoflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arsdevstudio.memoflow.data.local.dao.GratitudeDao
import com.arsdevstudio.memoflow.data.local.dao.NoteDao
import com.arsdevstudio.memoflow.data.local.dao.UserDao
import com.arsdevstudio.memoflow.data.local.entity.GratitudeEntity
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.data.local.entity.UserEntity

@Database(
    entities = [NoteEntity::class, UserEntity::class, GratitudeEntity::class],
    version = 12, // Incrementado de 11 para 12 para adicionar userId
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun gratitudeDao(): GratitudeDao

    companion object {
        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Adiciona a coluna userId na tabela notes
                db.execSQL("ALTER TABLE notes ADD COLUMN userId TEXT NOT NULL DEFAULT 'local_user'")
                // Adiciona a coluna userId na tabela gratitudes
                db.execSQL("ALTER TABLE gratitudes ADD COLUMN userId TEXT NOT NULL DEFAULT 'local_user'")
            }
        }
    }
}

