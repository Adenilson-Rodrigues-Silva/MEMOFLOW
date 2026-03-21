package com.example.memoflow.ui.screens.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.MemoApplication
import com.example.memoflow.data.local.entity.GratitudeEntity
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.local.entity.UserEntity
import com.example.memoflow.data.repository.MemoRepository
import com.example.memoflow.utils.BillingPrefs
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
            
            delay(3000)

            try {
                val notes = repository.allNotes.first()
                val gratitudes = repository.allGratitudes.first()
                val userSettings = repository.userSettings.first()

                val backupData = BackupData(notes, gratitudes, userSettings)
                val gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
                val jsonString = gson.toJson(backupData)

                val backupDir = File(context.cacheDir, "backups")
                if (!backupDir.exists()) backupDir.mkdirs()

                val fileName = "memoflow_backup_${System.currentTimeMillis()}.json"
                val file = File(backupDir, fileName)
                
                FileOutputStream(file).use { it.write(jsonString.toByteArray()) }

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                onFileReady(contentUri)
                _uiState.value = BackupUiState.Success("Backup preparado para exportação!")
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = BackupUiState.Error("Erro ao exportar: ${e.message}")
            }
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            
            delay(3000)

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = InputStreamReader(inputStream)
                    val backupData = Gson().fromJson(reader, BackupData::class.java)

                    if (backupData != null) {
                        repository.deleteAllNotes()
                        repository.deleteAllGratitudes()

                        backupData.notes.forEach { repository.insertNote(it) }
                        backupData.gratitudes.forEach { repository.insertGratitude(it) }
                        backupData.userSettings?.let { repository.saveUserSettings(it) }

                        _uiState.value = BackupUiState.Success("Restauração concluída!")
                    } else {
                        _uiState.value = BackupUiState.Error("Arquivo inválido.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error("Erro na importação: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = BackupUiState.Idle
    }

    sealed class BackupUiState {
        object Idle : BackupUiState()
        object Loading : BackupUiState()
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
