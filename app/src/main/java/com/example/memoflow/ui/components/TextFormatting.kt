package com.example.memoflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState

@Composable
fun TextFormattingPanel(
    state: RichTextState,
    isVisible: Boolean
) {
    if (isVisible) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Negrito
                ControlIcon(
                    onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                    icon = Icons.Default.FormatBold,
                    isActive = state.currentSpanStyle.fontWeight == FontWeight.Bold
                )
                // Itálico
                ControlIcon(
                    onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                    icon = Icons.Default.FormatItalic,
                    isActive = state.currentSpanStyle.fontStyle == FontStyle.Italic
                )
                // Underline (Sublinhado)
                ControlIcon(
                    onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                    icon = Icons.Default.FormatUnderlined,
                    isActive = state.currentSpanStyle.textDecoration == TextDecoration.Underline
                )
                // LISTA (Unordered List) - Nativo do RichTextState
                ControlIcon(
                    onClick = { state.toggleUnorderedList() },
                    icon = Icons.Default.FormatListBulleted,
                    isActive = state.isUnorderedList
                )
                // CITAÇÃO (Code/Quote) - Usando o SpanStyle para simular o bloco
                ControlIcon(
                    onClick = {
                        // No RichTextState, o toggleCode aplica o estilo de bloco de citação/código
                        state.toggleCode()
                    },
                    icon = Icons.Default.FormatQuote,
                    isActive = state.isCode
                )
            }
        }
    }
}

@Composable
fun ControlIcon(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}