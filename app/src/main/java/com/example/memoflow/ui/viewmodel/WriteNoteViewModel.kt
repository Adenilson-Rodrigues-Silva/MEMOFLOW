package com.example.memoflow.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.ViewModel
import com.mohamedrejeb.richeditor.model.RichTextState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import android.media.MediaRecorder
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.memoflow.data.repository.MemoRepository
import com.example.memoflow.data.local.entity.NoteEntity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

data class WriteNoteUiState(
    val id: Long = 0,
    val diaryEmojis: List<Pair<String, String>> = listOf(
        "😭" to "Muito Triste",
        "😢" to "Triste",
        "😐" to "Neutro",
        "😊" to "Feliz",
        "🤩" to "Muito Feliz",
        "😫" to "Estressado",
        "😡" to "Bravo"
    ),
    val title: String = "Hoje",
    val selectedEmoji: String = "😊",
    val selectedHumor: String = "Feliz",
    val images: List<Uri> = emptyList(),
    val audioUri: Uri? = null,
    val isRecording: Boolean = false,
    val recordingTime: Int = 0,
    val audioPath: String? = null,
    val isLocked: Boolean = false
)

class WriteNoteViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(WriteNoteUiState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    val richTextState = RichTextState()

    private var progressJob: Job? = null
    var isPlaying by mutableStateOf(false)
        private set

    private var mediaPlayer: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var timerJob: Job? = null
    private var tempPath: String? = null

    fun loadNote(noteId: Long) {
        if (noteId <= 0) return
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            note?.let {
                _uiStateFlow.update { currentState ->
                    currentState.copy(
                        id = it.id,
                        title = it.title,
                        selectedEmoji = it.emoji,
                        selectedHumor = it.humor,
                        images = it.images.map { uriString -> Uri.parse(uriString) },
                        audioPath = it.audioPath,
                        isLocked = it.isLocked
                    )
                }
                richTextState.setHtml(it.contentHtml)
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiStateFlow.update { it.copy(title = newTitle) }
    }

    fun updateEmoji(emoji: String, humor: String) {
        _uiStateFlow.update { it.copy(selectedEmoji = emoji, selectedHumor = humor) }
    }

    fun toggleLock() {
        _uiStateFlow.update { it.copy(isLocked = !it.isLocked) }
    }

    fun applyMarker(color: Color) {
        richTextState.toggleSpanStyle(SpanStyle(background = color.copy(alpha = 0.3f)))
    }

    fun updateTextColor(color: Color) {
        richTextState.toggleSpanStyle(SpanStyle(color = color.copy(alpha = 1f)))
    }

    fun updateFontFamily(fontFamily: FontFamily) {
        richTextState.toggleSpanStyle(SpanStyle(fontFamily = fontFamily))
    }

    fun saveNote() {
        viewModelScope.launch {
            val state = _uiStateFlow.value
            val note = NoteEntity(
                id = state.id,
                title = state.title,
                contentHtml = richTextState.toHtml(),
                emoji = state.selectedEmoji,
                humor = state.selectedHumor,
                date = if (state.id == 0L) System.currentTimeMillis() else repository.getNoteById(state.id)?.date ?: System.currentTimeMillis(),
                images = state.images.map { it.toString() },
                audioPath = state.audioPath,
                isLocked = state.isLocked
            )
            repository.insertNote(note)
        }
    }

    fun onVoiceClick(cacheDir: File) {
        if (_uiStateFlow.value.isRecording) {
            stopRecording()
        } else {
            startRecording(cacheDir)
        }
    }

    private fun startRecording(cacheDir: File) {
        val audioFile = File(cacheDir, "audio_${System.currentTimeMillis()}.mp3")
        tempPath = audioFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(tempPath)

            try {
                prepare()
                start()
                _uiStateFlow.update { it.copy(isRecording = true, recordingTime = 0) }
                startTimer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            _uiStateFlow.update { it.copy(isRecording = false, audioPath = tempPath) }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiStateFlow.update { it.copy(isRecording = false) }
        } finally {
            mediaRecorder = null
            timerJob?.cancel()
        }
    }

    fun playAudio() {
        val path = _uiStateFlow.value.audioPath
        if (path == null || _uiStateFlow.value.isRecording) return

        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            this@WriteNoteViewModel.isPlaying = false
            progressJob?.cancel()
            return
        }

        if (mediaPlayer != null && !this@WriteNoteViewModel.isPlaying) {
            mediaPlayer?.start()
            this@WriteNoteViewModel.isPlaying = true
            startProgressUpdate()
            return
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener {
                    this@WriteNoteViewModel.isPlaying = true
                    start()
                    startProgressUpdate()
                }
                setOnCompletionListener {
                    this@WriteNoteViewModel.isPlaying = false
                    progressJob?.cancel()
                    _uiStateFlow.update { it.copy(recordingTime = 0) }
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            this@WriteNoteViewModel.isPlaying = false
        }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (this@WriteNoteViewModel.isPlaying) {
                mediaPlayer?.let {
                    val currentSec = it.currentPosition / 1000
                    _uiStateFlow.update { state -> state.copy(recordingTime = currentSec) }
                }
                delay(500)
            }
        }
    }

    fun deleteAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _uiStateFlow.value.audioPath?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
        _uiStateFlow.update { it.copy(audioPath = null, recordingTime = 0) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiStateFlow.value.recordingTime < 30 && _uiStateFlow.value.isRecording) {
                delay(1000)
                _uiStateFlow.update { it.copy(recordingTime = it.recordingTime + 1) }
            }
            if (_uiStateFlow.value.recordingTime >= 30) {
                stopRecording()
            }
        }
    }

    fun onImageSelected(uri: Uri?) {
        uri?.let { selectedUri ->
            _uiStateFlow.update { currentState ->
                if (currentState.images.size < 3) {
                    currentState.copy(images = currentState.images + selectedUri)
                } else {
                    currentState
                }
            }
        }
    }

    fun removeImage(uri: Uri) {
        _uiStateFlow.update { currentState ->
            val currentList = currentState.images.toMutableList()
            currentList.remove(uri)
            currentState.copy(images = currentList)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.memoflow.MemoApplication
                return WriteNoteViewModel(application.repository) as T
            }
        }
    }
}
