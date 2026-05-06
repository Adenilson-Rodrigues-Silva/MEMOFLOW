package com.arsdevstudio.memoflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState

@Composable
fun TextFormattingPanel(
    state: RichTextState,
    isVisible: Boolean,
    neonGreen: Color = Color(0xFF00FFC2)
) {
    if (isVisible) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFF1E1E1E),
            shape = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // LINHA 1: Estilos Básicos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlIcon(
                        onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                        icon = Icons.Default.FormatBold,
                        isActive = state.currentSpanStyle.fontWeight == FontWeight.Bold,
                        activeColor = neonGreen
                    )
                    ControlIcon(
                        onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                        icon = Icons.Default.FormatItalic,
                        isActive = state.currentSpanStyle.fontStyle == FontStyle.Italic,
                        activeColor = neonGreen
                    )
                    ControlIcon(
                        onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                        icon = Icons.Default.FormatUnderlined,
                        isActive = state.currentSpanStyle.textDecoration == TextDecoration.Underline,
                        activeColor = neonGreen
                    )
                    
                    VerticalDivider(modifier = Modifier.height(24.dp), color = Color.Gray.copy(alpha = 0.3f))

                    // TAMANHOS DE FONTE (P, M, G)
                    FontSizeButton(label = "P", size = 14, state = state, activeColor = neonGreen)
                    FontSizeButton(label = "M", size = 18, state = state, activeColor = neonGreen)
                    FontSizeButton(label = "G", size = 24, state = state, activeColor = neonGreen)
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // LINHA 2: Listas e Blocos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ControlIcon(
                        onClick = { state.toggleUnorderedList() },
                        icon = Icons.Default.FormatListBulleted,
                        isActive = state.isUnorderedList,
                        activeColor = neonGreen
                    )
                    ControlIcon(
                        onClick = { state.toggleOrderedList() },
                        icon = Icons.Default.FormatListNumbered,
                        isActive = state.isOrderedList,
                        activeColor = neonGreen
                    )
                    ControlIcon(
                        onClick = { state.toggleCode() },
                        icon = Icons.Default.FormatQuote,
                        isActive = state.isCode,
                        activeColor = neonGreen
                    )
                }
            }
        }
    }
}

@Composable
fun FontSizeButton(label: String, size: Int, state: RichTextState, activeColor: Color) {
    val isSelected = state.currentSpanStyle.fontSize == size.sp
    
    TextButton(
        onClick = { state.toggleSpanStyle(SpanStyle(fontSize = size.sp)) },
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(36.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) activeColor else Color.Gray,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ControlIcon(
    onClick: () -> Unit, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    isActive: Boolean,
    activeColor: Color
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) activeColor else Color.Gray
        )
    }
}

