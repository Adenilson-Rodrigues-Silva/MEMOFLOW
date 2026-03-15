package com.example.memoflow

import android.app.Application
import androidx.room.Room
import com.example.memoflow.data.local.MemoDatabase
import com.example.memoflow.data.repository.MemoRepository
import com.example.memoflow.utils.NotificationHelper

class MemoApplication : Application() {
    
    val database by lazy { 
        Room.databaseBuilder(
            this,
            MemoDatabase::class.java,
            "memo_flow_db"
        )
        .fallbackToDestructiveMigration()
        .build() 
    }

    val repository by lazy { 
        MemoRepository(database.noteDao(), database.userDao(), database.gratitudeDao())
    }

    override fun onCreate() {
        super.onCreate()
        // ✅ CRUCIAL: Cria os canais de notificação assim que o app inicia
        NotificationHelper(this).createNotificationChannels()
    }
}
