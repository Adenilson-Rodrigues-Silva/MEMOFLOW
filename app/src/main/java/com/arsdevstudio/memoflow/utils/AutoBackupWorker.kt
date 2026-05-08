package com.arsdevstudio.memoflow.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arsdevstudio.memoflow.MemoApplication
import com.arsdevstudio.memoflow.ui.screens.profile.BackupData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.flow.first

class AutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as MemoApplication
        
        // 1. Só faz backup se for Premium
        val isPremium = app.billingPrefs.isPremium.first()
        if (!isPremium) return Result.success()

        // 2. Só faz backup se estiver logado no Google
        val account = GoogleSignIn.getLastSignedInAccount(applicationContext) ?: return Result.failure()
        
        val driveServiceHelper = GoogleDriveService(applicationContext)
        if (!driveServiceHelper.hasDrivePermission()) {
            Log.w("AutoBackup", "Sem permissão de Drive")
            return Result.retry() // Tenta mais tarde quando tiver permissão
        }

        return try {
            val driveService = driveServiceHelper.getDriveService(account)
            val backupManager = GoogleDriveBackupManager(applicationContext)

            // 3. Coleta os dados atuais
            val user = app.repository.userSettings.first()
            val userId = if (user?.isGoogleLogged == true) user.firebaseUid ?: "" else ""
            if (userId.isEmpty()) return Result.failure()
            
            val notes = app.repository.getAllNotes(userId).first()
            val gratitudes = app.repository.getAllGratitudes(userId).first()
            val userSettings = user
            
            val backupData = BackupData(notes, gratitudes, userSettings)

            // 4. Sobe para a nuvem
            val success = backupManager.uploadBackup(driveService, backupData)
            
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Log.e("AutoBackup", "Falha no backup automático", e)
            Result.retry()
        }
    }
}

