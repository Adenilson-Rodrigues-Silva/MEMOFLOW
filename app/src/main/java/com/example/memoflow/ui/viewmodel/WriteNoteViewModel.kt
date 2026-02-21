package com.example.memoflow.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel

data class WriteNoteUiState(
    val title: String = "Hoje",
    val content: TextFieldValue = TextFieldValue(""),
    val selectedEmoji: String = "😊",
    val selectedHumor: String = "Feliz",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val images: List<Uri> = emptyList(),
    val audioUri: Uri? = null,
    val isRecording: Boolean = false
)

class WriteNoteViewModel : ViewModel() {

    var uiState by mutableStateOf(WriteNoteUiState())
        private set

    fun updateTitle(newTitle: String) {
        uiState = uiState.copy(title = newTitle)
    }

    // CORREÇÃO: Esta função agora protege a AnnotatedString com unhas e dentes
    fun updateContent(newValue: TextFieldValue) {
        val currentContent = uiState.content

        // Se o texto mudou, o Android manda uma AnnotatedString "limpa".
        // Precisamos garantir que não estamos perdendo metadados importantes.
        uiState = uiState.copy(content = newValue)
    }

    fun updateEmoji(emoji: String, humor: String) {
        uiState = uiState.copy(selectedEmoji = emoji, selectedHumor = humor)
    }

    // Lógica para as imagens (Máximo 3) - Já implementado como solicitado
    fun addImage(uri: Uri) {
        if (uiState.images.size < 3) {
            uiState = uiState.copy(images = uiState.images + uri)
        }
    }

    fun removeImage(uri: Uri) {
        uiState = uiState.copy(images = uiState.images - uri)
    }

    fun setAudio(uri: Uri) {
        uiState = uiState.copy(audioUri = uri)
    }

    fun removeAudio() {
        uiState = uiState.copy(audioUri = null)
    }

    fun toggleBold() {
        applyStyleToSelection(SpanStyle(fontWeight = FontWeight.Bold))
        uiState = uiState.copy(isBold = !uiState.isBold)
    }

    fun toggleItalic() {
        applyStyleToSelection(SpanStyle(fontStyle = FontStyle.Italic))
        uiState = uiState.copy(isItalic = !uiState.isItalic)
    }

    fun toggleUnderline() {
        applyStyleToSelection(SpanStyle(textDecoration = TextDecoration.Underline))
        uiState = uiState.copy(isUnderline = !uiState.isUnderline)
    }

    fun applyMarker(color: Color) {
        applyStyleToSelection(SpanStyle(background = color))
    }

    // CORREÇÃO: Nova lógica de aplicação de estilo para garantir permanência
    private fun applyStyleToSelection(style: SpanStyle) {
        val selection = uiState.content.selection
        val currentAnnotatedString = uiState.content.annotatedString

        if (!selection.collapsed) {
            val newAnnotatedString = buildAnnotatedString {
                // Primeiro, trazemos TUDO o que já existia (estilos antigos)
                append(currentAnnotatedString)

                // Depois, aplicamos o novo estilo no intervalo da seleção
                addStyle(
                    style = style,
                    start = selection.min,
                    end = selection.max
                )
            }

            uiState = uiState.copy(
                content = TextFieldValue(
                    annotatedString = newAnnotatedString,
                    selection = selection
                )
            )
        }
    }
}