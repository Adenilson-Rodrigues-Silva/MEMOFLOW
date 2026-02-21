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


data class WriteNoteUiState(
    val title: String = "Hoje",
    val selectedEmoji: String = "😊",
    val selectedHumor: String = "Feliz",
    val images: List<Uri> = emptyList(),
    val audioUri: Uri? = null,
    val isRecording: Boolean = false
)

class WriteNoteViewModel : ViewModel() {

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
        uiState = uiState.copy(selectedEmoji = emoji, selectedHumor = humor)
    }

    // Lógica para as imagens (Máximo 3)
    fun addImage(uri: Uri) {
        if (uiState.images.size < 3) {
            uiState = uiState.copy(images = uiState.images + uri)
        }
    }

    fun removeImage(uri: Uri) {
        uiState = uiState.copy(images = uiState.images - uri)
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
        // Aplica a cor de fundo neon que escolhemos
        richTextState.toggleSpanStyle(SpanStyle(background = color))
    }
}