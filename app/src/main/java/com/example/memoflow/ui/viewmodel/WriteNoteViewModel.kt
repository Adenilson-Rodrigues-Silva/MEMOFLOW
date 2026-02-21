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
        richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic, background = Color.White.copy(alpha = 0.1f)))
    }
}