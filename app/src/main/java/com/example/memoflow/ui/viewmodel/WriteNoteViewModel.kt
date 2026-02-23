package com.example.memoflow.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.ViewModel
import com.mohamedrejeb.richeditor.model.RichTextState // CERTIFIQUE-SE DESTE IMPORT


import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
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
    val recordingTime: Int = 0, // Tempo em segundos (0 a 30)
    val audioPath: String? = null // Caminho do arquivo gravado




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


    // ... dentro da classe WriteNoteViewModel
    private var mediaPlayer: android.media.MediaPlayer? = null

    private var mediaRecorder: MediaRecorder? = null
    private var timerJob: Job? = null

    private var tempPath: String? = null // Variável temporária

    // 1. O Estado da UI (título, emojis, imagens)
    var uiState by mutableStateOf(WriteNoteUiState())
        private set

    // 2. O ESTADO DO RICH TEXT (Faltava isto aqui!)
    // Ele é um val porque o objeto em si não muda, apenas o seu conteúdo interno
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

    // Lógica de Imagem com trava de 3
    fun addImage(uri: Uri) {
        if (uiState.images.size < 3) {
            uiState = uiState.copy(images = uiState.images + uri)
        }
    }

    fun removeImageAtIndex(index: Int) {
        val newList = uiState.images.toMutableList()
        if (index in newList.indices) {
            newList.removeAt(index) // Remove apenas o slot específico [cite: 2026-02-08]
            uiState = uiState.copy(images = newList)
        }
    }

    // --- FUNÇÕES QUE ESTAVAM A VERMELHO ---

    // --- FUNÇÕES CORRIGIDAS ---

    fun toggleBold() {
        // Usamos o SpanStyle nativo para aplicar o Negrito
        richTextState.toggleSpanStyle(
            SpanStyle(fontWeight = FontWeight.Bold)
        )
    }

    fun toggleItalic() {
        // Usamos o SpanStyle nativo para aplicar o Itálico
        richTextState.toggleSpanStyle(
            SpanStyle(fontStyle = FontStyle.Italic)
        )
    }

    fun toggleUnderline() {
        // Usamos o SpanStyle nativo para aplicar o Sublinhado
        richTextState.toggleSpanStyle(
            SpanStyle(textDecoration = TextDecoration.Underline)
        )
    }

    fun applyMarker(color: Color) {
        // Cor suave (Alpha 0.3) para o fundo (marcador)
        richTextState.toggleSpanStyle(SpanStyle(background = color.copy(alpha = 0.3f)))
    }
    // --- NOVAS FUNÇÕES DE TEXTO ---

    // 1. Listas (Bullets e Números)
    fun toggleBulletList() = richTextState.toggleUnorderedList()
    fun toggleOrderedList() = richTextState.toggleOrderedList()

    // 2. Tamanhos de Fonte (Sugestão: 14sp, 18sp, 24sp)
    fun updateFontSize(size: androidx.compose.ui.unit.TextUnit) {
        richTextState.toggleSpanStyle(SpanStyle(fontSize = size))
    }

    // 3. Cores da Fonte (As mesmas do marcador, mas para a letra)
    fun updateTextColor(color: Color) {
        // Cor forte (Alpha 1.0) para a letra
        richTextState.toggleSpanStyle(SpanStyle(color = color.copy(alpha = 1f)))
    }

    // 4. Citação (Blockquote)
// A biblioteca costuma usar um recuo ou estilo de parágrafo para isso
    fun toggleQuote() {
        // Podemos simular uma citação com itálico + recuo ou usar o método da lib se disponível
        richTextState.toggleSpanStyle(
            SpanStyle(
                fontStyle = FontStyle.Italic,
                background = Color.White.copy(alpha = 0.1f)
            )
        )
    }



    fun saveNote() {
        // Acessamos o texto através do uiState [cite: 2026-02-08]
        val textoDaNota = uiState.richTextState.annotatedString.text
        val emojiSelecionado = uiState.selectedEmoji
        val quantidadeImagens = uiState.images.size

        // Log para você ver no console do Android Studio que os dados estão corretos
        println("--- SALVANDO NO CHRONOS DIARY ---")
        println("Texto: $textoDaNota")
        println("Humor Index: $emojiSelecionado")
        println("Imagens anexadas: $quantidadeImagens")

        // Aqui no futuro faremos o INSERT no banco de dados [cite: 2026-02-08]
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
        tempPath = audioFile.absolutePath // Guarda no temporário primeiro

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(tempPath)

            try {
                prepare()
                start()
                // NÃO salvamos o audioPath no uiState ainda! [cite: 2026-02-08]
                uiState = uiState.copy(
                    isRecording = true,
                    recordingTime = 0
                    // audioPath continua null aqui
                )
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
            // SÓ AGORA o áudio existe de fato, então passamos para o estado [cite: 2026-02-08]
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

        // 1. LÓGICA DE PAUSE: Se já está tocando, apenas pausa
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            this@WriteNoteViewModel.isPlaying = false
            progressJob?.cancel() // Para de atualizar a barra enquanto pausado
            return
        }

        // 2. LÓGICA DE RESUME: Se estava pausado, apenas continua
        if (mediaPlayer != null && !this@WriteNoteViewModel.isPlaying) {
            mediaPlayer?.start()
            this@WriteNoteViewModel.isPlaying = true
            startProgressUpdate() // Volta a atualizar a barra
            return
        }

        // 3. LÓGICA DE START: Começa do zero
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
                    // Opcional: Voltar o tempo para zero quando acabar
                    uiState = uiState.copy(recordingTime = 0)
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            this@WriteNoteViewModel.isPlaying = false
            android.util.Log.e("CHRONOS", "Erro: ${e.message}")
        }
    }

    // Função auxiliar para fazer a barra "andar"
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
        // 1. Paramos o áudio se ele estiver tocando
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // 2. Apagamos o arquivo físico se ele existir [cite: 2026-02-08]
        uiState.audioPath?.let { path ->
            val file = java.io.File(path)
            if (file.exists()) file.delete()
        }

        // 3. Resetamos o estado na UI para permitir nova gravação [cite: 2026-02-08]
        uiState = uiState.copy(
            audioPath = null,
            recordingTime = 0
        )
    }

    private fun startTimer() {
        timerJob?.cancel() // Cancela qualquer cronômetro antigo antes de começar
        timerJob = viewModelScope.launch {
            while (uiState.recordingTime < 30 && uiState.isRecording) {
                delay(1000)
                // Incrementamos o tempo atual criando uma nova cópia do estado
                uiState = uiState.copy(recordingTime = uiState.recordingTime + 1)
            }

            // Se atingir 30 segundos, para a gravação automaticamente
            if (uiState.recordingTime >= 30) {
                stopRecording()
            }
        }
    }



    fun onImageSelected(uri: Uri?) {
        uri?.let { selectedUri ->
            _uiState.update { currentState ->
                if (currentState.images.size < 3) {
                    // Adiciona a nova imagem à lista atual [cite: 2026-02-08]
                    currentState.copy(images = currentState.images + selectedUri)
                } else {
                    currentState
                }
            }
        }
    }

    fun removeImage(uri: android.net.Uri) {
        _uiState.update { currentState ->
            // Criamos uma lista nova, mas removemos apenas a PRIMEIRA ocorrência dessa URI
            val currentList = currentState.images.toMutableList()
            currentList.remove(uri)

            currentState.copy(images = currentList)
        }
    }

}




