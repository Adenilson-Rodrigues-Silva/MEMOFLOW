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
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory


class MemoApplication : Application() {
    
    val database by lazy { 
        // Recupera a senha (ByteArray) gerada pelo KeyStore para o SQLCipher
        val passphrase = SecurityUtils.getDatabasePassphrase(this)
        val factory = SupportFactory(passphrase)


        Room.databaseBuilder(
            this,
            MemoDatabase::class.java,
            "memo_flow_db"
        )
        .openHelperFactory(factory)
        .addMigrations(
            MemoDatabase.MIGRATION_11_12, 
            MemoDatabase.MIGRATION_12_13,
            MemoDatabase.MIGRATION_13_14
        )
        .fallbackToDestructiveMigration()
        .build() 
    }

    val repository by lazy { 
        MemoRepository(
            database.noteDao(), 
            database.userDao(), 
            database.gratitudeDao(),
            database.notificationDao()
        )
    }

    val billingPrefs by lazy { BillingPrefs(this) }
    val aiPrefs by lazy { AiPrefs(this) }
    val billingManager by lazy { BillingManager(this, billingPrefs, repository) }

    override fun onCreate() {
        super.onCreate()
        // ✅ Inicializa o SQLCipher
        SQLiteDatabase.loadLibs(this)

        // ✅ Inicializa recursos essenciais do app
        NotificationHelper(this).createNotificationChannels()
        billingManager.startConnection()
    }
}
