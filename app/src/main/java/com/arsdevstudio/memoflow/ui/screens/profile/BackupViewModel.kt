package com.arsdevstudio.memoflow.ui.screens.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.arsdevstudio.memoflow.MemoApplication
import com.arsdevstudio.memoflow.data.local.entity.GratitudeEntity
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.data.local.entity.UserEntity
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import com.arsdevstudio.memoflow.utils.BillingPrefs
import com.arsdevstudio.memoflow.utils.GoogleDriveBackupManager
import com.arsdevstudio.memoflow.utils.GoogleDriveService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

data class BackupData(
    val notes: List<NoteEntity>,
    val gratitudes: List<GratitudeEntity>,
    val userSettings: UserEntity?,
    val version: Int = 2,
    val timestamp: Long = System.currentTimeMillis()
)

class BackupViewModel(
    application: Application,
    private val repository: MemoRepository,
    private val billingPrefs: BillingPrefs
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    val isPremium: StateFlow<Boolean> = billingPrefs.isPremium.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    fun exportBackup(context: Context, onFileReady: (Uri) -> Unit) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            delay(2000)
            try {
                val notes = repository.allNotes.first()
                val gratitudes = repository.allGratitudes.first()
                val userSettings = repository.userSettings.first()
                val backupData = BackupData(notes, gratitudes, userSettings)
                val gson = com.google.gson.GsonBuilder().serializeNulls().setPrettyPrinting().create()
                val jsonString = gson.toJson(backupData)
                val backupDir = File(context.cacheDir, "backups").apply { if (!exists()) mkdirs() }
                val file = File(backupDir, "memoflow_backup_${System.currentTimeMillis()}.json")
                FileOutputStream(file).use { it.write(jsonString.toByteArray()) }
                val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                onFileReady(contentUri)
                _uiState.value = BackupUiState.Success("Backup manual pronto!")
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error("Erro ao exportar.")
            }
        }
    }

    fun uploadToDriveManual(context: Context) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.DriveLoading
            val startTime = System.currentTimeMillis()
            try {
                val account = GoogleSignIn.getLastSignedInAccount(context) 
                    ?: throw Exception("Conta Google não vinculada. Clique em 'Restaurar' primeiro.")
                
                val driveServiceHelper = GoogleDriveService(context)
                val driveService = driveServiceHelper.getDriveService(account)
                val backupManager = GoogleDriveBackupManager(context)

                val notes = repository.allNotes.first()
                val gratitudes = repository.allGratitudes.first()
                val userSettings = repository.userSettings.first()
                val backupData = BackupData(notes, gratitudes, userSettings)

                val success = backupManager.uploadBackup(driveService, backupData)
                
                ensureMinDelay(startTime)
                if (success) {
                    _uiState.value = BackupUiState.Success("Sincronizado com sucesso! ✨")
                } else {
                    _uiState.value = BackupUiState.Error("O Google recusou o arquivo. Verifique o SHA-1 no console.")
                }
            } catch (e: com.google.api.client.googleapis.json.GoogleJsonResponseException) {
                ensureMinDelay(startTime)
                _uiState.value = BackupUiState.Error("Erro Google (${e.statusCode}): ${e.details?.message ?: "Acesso Negado"}")
            } catch (e: Exception) {
                ensureMinDelay(startTime)
                _uiState.value = BackupUiState.Error(e.message ?: "Erro desconhecido")
                Log.e("DriveBackup", "Erro no upload", e)
            }
        }
    }

    fun restoreFromDrive(context: Context) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.DriveLoading
            val startTime = System.currentTimeMillis()

            try {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account == null) {
                    ensureMinDelay(startTime)
                    _uiState.value = BackupUiState.Error("Login Google não detectado.")
                    return@launch
                }

                val driveServiceHelper = GoogleDriveService(context)
                val driveService = driveServiceHelper.getDriveService(account)
                val backupManager = GoogleDriveBackupManager(context)

                val backupData = backupManager.downloadBackup(driveService)

                ensureMinDelay(startTime)

                if (backupData != null) {
                    applyBackup(backupData)
                    _uiState.value = BackupUiState.Success("Flow restaurado da nuvem!")
                } else {
                    _uiState.value = BackupUiState.Error("Nenhum backup automático encontrado.")
                }
            } catch (e: com.google.api.client.googleapis.json.GoogleJsonResponseException) {
                ensureMinDelay(startTime)
                _uiState.value = BackupUiState.Error("Erro Google (${e.statusCode}): Verifique as permissões.")
            } catch (e: Exception) {
                ensureMinDelay(startTime)
                _uiState.value = BackupUiState.Error("Erro na nuvem: ${e.message}")
            }
        }
    }

    private suspend fun ensureMinDelay(startTime: Long) {
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed < 5300) delay(5300 - elapsed)
    }

    private suspend fun applyBackup(data: BackupData) {
        repository.deleteAllNotes()
        repository.deleteAllGratitudes()
        data.notes.forEach { repository.insertNote(it) }
        data.gratitudes.forEach { repository.insertGratitude(it) }
        data.userSettings?.let { repository.saveUserSettings(it) }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            delay(2000)
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val backupData = Gson().fromJson(InputStreamReader(inputStream), BackupData::class.java)
                    applyBackup(backupData)
                    _uiState.value = BackupUiState.Success("Restauração local concluída!")
                }
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error("Arquivo inválido.")
            }
        }
    }

    fun resetState() { _uiState.value = BackupUiState.Idle }

    sealed class BackupUiState {
        object Idle : BackupUiState()
        object Loading : BackupUiState()
        object DriveLoading : BackupUiState()
        data class Success(val message: String) : BackupUiState()
        data class Error(val message: String) : BackupUiState()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as MemoApplication
                return BackupViewModel(application, application.repository, application.billingPrefs) as T
            }
        }
    }
}

