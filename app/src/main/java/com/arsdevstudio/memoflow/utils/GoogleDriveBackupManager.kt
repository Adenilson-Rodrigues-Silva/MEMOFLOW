package com.arsdevstudio.memoflow.utils

import android.content.Context
import android.util.Log
import com.arsdevstudio.memoflow.ui.screens.profile.BackupData
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleDriveBackupManager(private val context: Context) {

    private val TAG = "DriveBackup"
    private val BACKUP_FILE_NAME = "memoflow_auto_backup.json"

    /**
     * Faz o upload do backup atual para o Google Drive.
     * Se já existir um arquivo com o mesmo nome, ele o substitui.
     */
    suspend fun uploadBackup(driveService: Drive, backupData: BackupData): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = backupData.userSettings?.firebaseUid ?: ""
            if (userId.isEmpty()) return@withContext false

            val jsonString = Gson().toJson(backupData)
            
            // CRIPTOGRAFIA DE PONTA A PONTA
            val encryptedData = SecurityUtils.encryptBackup(jsonString, userId)
            
            val tempFile = java.io.File(context.cacheDir, BACKUP_FILE_NAME)
            tempFile.writeText(encryptedData)

            // 1. Procura se já existe um backup anterior
            val existingFileId = findBackupFile(driveService)

            val metadata = File().apply {
                name = BACKUP_FILE_NAME
                parents = listOf("appDataFolder") // Pasta oculta e segura
            }

            val mediaContent = FileContent("text/plain", tempFile)

            if (existingFileId != null) {
                // Atualiza o existente
                driveService.files().update(existingFileId, null, mediaContent).execute()
                Log.d(TAG, "Backup criptografado atualizado no Drive!")
            } else {
                // Cria um novo
                driveService.files().create(metadata, mediaContent).execute()
                Log.d(TAG, "Novo backup criptografado criado no Drive!")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer upload criptografado para o Drive", e)
            false
        }
    }

    /**
     * Procura o ID do arquivo de backup na pasta AppData.
     */
    private fun findBackupFile(driveService: Drive): String? {
        val result = driveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILE_NAME'")
            .setFields("files(id)")
            .execute()

        return result.files?.firstOrNull()?.id
    }

    /**
     * Baixa o backup do Drive e retorna o objeto BackupData.
     */
    suspend fun downloadBackup(driveService: Drive, userId: String): BackupData? = withContext(Dispatchers.IO) {
        try {
            val fileId = findBackupFile(driveService) ?: return@withContext null
            
            val outputStream = java.io.ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            
            val encryptedData = outputStream.toString()
            
            // DESCRIPTOGRAFIA
            val decryptedJson = SecurityUtils.decryptBackup(encryptedData, userId)
            
            Gson().fromJson(decryptedJson, BackupData::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao baixar ou descriptografar backup do Drive", e)
            null
        }
    }
}

