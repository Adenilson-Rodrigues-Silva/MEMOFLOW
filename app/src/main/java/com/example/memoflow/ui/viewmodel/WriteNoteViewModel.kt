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
import kotlinx.coroutines.flow.update


data class WriteNoteUiState(

    val diaryEmojis: List<Pair<String, String>> = listOf(
        "😭" to "Muito Triste",
        "😢" to "Triste",
        "😐" to "Neutro",
        "😊" to "Feliz",
        "🤩" to "Muito Feliz",
        "😫" to "Estressado",
        "😡" to "Bravo"
    ),

    val richTextState: RichTextState = RichTextState(),
    val title: String = "Hoje",
    val selectedEmoji: String = "😊",
    val selectedHumor: String = "Feliz",
    val images: List<Uri> = emptyList(),
    val audioUri: Uri? = null,
    val isRecording: Boolean = false,
    val recordingTime: Int = 0,
    val audioPath: String? = null
)

class WriteNoteViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WriteNoteUiState())
    val uiStateFlow = _uiState

    private var state: WriteNoteUiState
        get() = _uiState.value
        set(value) { _uiState.value = value }

    private var progressJob: Job? = null

    var isPlaying by mutableStateOf(false)
        private set

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var timerJob: Job? = null
    private var tempPath: String? = null

    var uiState by mutableStateOf(WriteNoteUiState())
        private set

    val richTextState = RichTextState()

    fun updateTitle(newTitle: String) {
        uiState = uiState.copy(title = newTitle)
    }

    fun updateEmoji(emoji: String, humor: String) {
        uiState = uiState.copy(
            selectedEmoji = emoji,
            selectedHumor = humor
        )
    }

    fun addImage(uri: Uri) {
        if (uiState.images.size < 3) {
            uiState = uiState.copy(images = uiState.images + uri)
        }
    }

    fun removeImageAtIndex(index: Int) {
        val newList = uiState.images.toMutableList()
        if (index in newList.indices) {
            newList.removeAt(index)
            uiState = uiState.copy(images = newList)
        }
    }

    fun toggleBold() {
        richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
    }

    fun toggleItalic() {
        richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
    }

    fun toggleUnderline() {
        richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
    }

    fun applyMarker(color: Color) {
        richTextState.toggleSpanStyle(SpanStyle(background = color.copy(alpha = 0.3f)))
    }

    fun toggleBulletList() = richTextState.toggleUnorderedList()
    fun toggleOrderedList() = richTextState.toggleOrderedList()

    fun updateFontSize(size: androidx.compose.ui.unit.TextUnit) {
        richTextState.toggleSpanStyle(SpanStyle(fontSize = size))
    }

    fun updateTextColor(color: Color) {
        richTextState.toggleSpanStyle(SpanStyle(color = color.copy(alpha = 1f)))
    }

    // AQUI ESTÁ A FUNÇÃO PARA ALTERAR A FONTE DALI PARA FRENTE
    fun updateFontFamily(fontFamily: FontFamily) {
        richTextState.toggleSpanStyle(SpanStyle(fontFamily = fontFamily))
    }

    fun toggleQuote() {
        richTextState.toggleSpanStyle(
            SpanStyle(
                fontStyle = FontStyle.Italic,
                background = Color.White.copy(alpha = 0.1f)
            )
        )
    }

    fun saveNote() {
        val textoDaNota = richTextState.annotatedString.text
        println("--- SALVANDO NO CHRONOS DIARY ---")
        println("Texto: $textoDaNota")
    }

    fun onVoiceClick(cacheDir: File) {
        if (uiState.isRecording) {
            stopRecording()
        } else {
            startRecording(cacheDir)
        }
    }

    private fun startRecording(cacheDir: File) {
        val audioFile = File(cacheDir, "temp_note_audio.mp3")
        tempPath = audioFile.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(tempPath)

            try {
                prepare()
                start()
                uiState = uiState.copy(isRecording = true, recordingTime = 0)
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
            uiState = uiState.copy(isRecording = false, audioPath = tempPath)
        } catch (e: Exception) {
            e.printStackTrace()
            uiState = uiState.copy(isRecording = false)
        } finally {
            mediaRecorder = null
            timerJob?.cancel()
        }
    }

    fun playAudio() {
        val path = uiState.audioPath
        if (path == null || uiState.isRecording) return

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
            mediaPlayer = android.media.MediaPlayer().apply {
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
                    uiState = uiState.copy(recordingTime = 0)
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
                    uiState = uiState.copy(recordingTime = currentSec)
                }
                delay(500)
            }
        }
    }

    fun deleteAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        uiState.audioPath?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) file.delete()
        }
        uiState = uiState.copy(audioPath = null, recordingTime = 0)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (uiState.recordingTime < 30 && uiState.isRecording) {
                delay(1000)
                uiState = uiState.copy(recordingTime = uiState.recordingTime + 1)
            }
            if (uiState.recordingTime >= 30) {
                stopRecording()
            }
        }
    }

    fun onImageSelected(uri: Uri?) {
        uri?.let { selectedUri ->
            _uiState.update { currentState ->
                if (currentState.images.size < 3) {
                    currentState.copy(images = currentState.images + selectedUri)
                } else {
                    currentState
                }
            }
        }
    }

    fun removeImage(uri: android.net.Uri) {
        _uiState.update { currentState ->
            val currentList = currentState.images.toMutableList()
            currentList.remove(uri)
            currentState.copy(images = currentList)
        }
    }
}
