package com.arsdevstudio.memoflow

import android.app.Application
import androidx.room.Room
import com.arsdevstudio.memoflow.data.local.MemoDatabase
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import com.arsdevstudio.memoflow.utils.AiPrefs
import com.arsdevstudio.memoflow.utils.BillingManager
import com.arsdevstudio.memoflow.utils.BillingPrefs
import com.arsdevstudio.memoflow.utils.NotificationHelper
import com.arsdevstudio.memoflow.utils.SecurityUtils
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

class MemoApplication : Application() {
    
    val database by lazy { 
        val factory = SupportOpenHelperFactory(SecurityUtils.getDatabasePassphrase(this))
        Room.databaseBuilder(
            this,
            MemoDatabase::class.java,
            "memo_flow_db"
        )
        .openHelperFactory(factory)
        .addMigrations(MemoDatabase.MIGRATION_11_12)
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

