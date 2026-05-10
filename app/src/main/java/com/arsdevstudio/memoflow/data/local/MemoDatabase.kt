package com.arsdevstudio.memoflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arsdevstudio.memoflow.data.local.dao.GratitudeDao
import com.arsdevstudio.memoflow.data.local.dao.NotificationDao
import com.arsdevstudio.memoflow.data.local.dao.NoteDao
import com.arsdevstudio.memoflow.data.local.dao.UserDao
import com.arsdevstudio.memoflow.data.local.entity.GratitudeEntity
import com.arsdevstudio.memoflow.data.local.entity.NotificationEntity
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.data.local.entity.UserEntity

@Database(
    entities = [NoteEntity::class, UserEntity::class, GratitudeEntity::class, NotificationEntity::class],
    version = 14, // Incrementado para adicionar isPremium ao UserEntity
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun gratitudeDao(): GratitudeDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_settings ADD COLUMN isPremium INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `notifications` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `message` TEXT NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `targetId` TEXT, 
                        `isRead` INTEGER NOT NULL, 
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

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

