package com.example.memoflow

import android.app.Application
import androidx.room.Room
import com.example.memoflow.data.local.MemoDatabase
import com.example.memoflow.data.repository.MemoRepository
import com.example.memoflow.utils.AiPrefs
import com.example.memoflow.utils.BillingManager
import com.example.memoflow.utils.BillingPrefs
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

    val billingPrefs by lazy { BillingPrefs(this) }
    val aiPrefs by lazy { AiPrefs(this) }
    val billingManager by lazy { BillingManager(this, billingPrefs) }

    override fun onCreate() {
        super.onCreate()
        // ✅ CRUCIAL: Cria os canais de notificação assim que o app inicia
        NotificationHelper(this).createNotificationChannels()
        
        // Inicializa o billing
        billingManager.startConnection()
    }
}
