package com.example.memoflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingColorMenu(
    selectedTextColor: Color,
    selectedMarkerColor: Color,
    onTextColorSelected: (Color) -> Unit,
    onMarkerColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    neonGreen: Color
) {
    val colors = listOf(neonGreen, Color(0xFFFF00E5), Color(0xFF00B2FF), Color(0xFFE6FB04), Color.White)

    Surface(
        color = Color(0xFF2A2A2A),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(16.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("COR DO TEXTO", color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colors.forEach { color ->
                    // Compara o código ARGB para garantir que a marcação "ande" com o Rich Text [cite: 2026-02-23]
                    val isSelected = isColorEqual(color, selectedTextColor, defaultMatch = Color.White)

                    ColorOption(color, isSelected, isMarker = false) {
                        onTextColorSelected(color)
                        onDismiss()
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("MARCADOR (FUNDO)", color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colors.forEach { color ->
                    val isSelected = isColorEqual(color, selectedMarkerColor, defaultMatch = Color.White)

                    ColorOption(color, isSelected, isMarker = true) {
                        onMarkerColorSelected(color)
                        onDismiss()
                    }
                }
            }
        }
    }
}

@Composable
fun ColorOption(color: Color, isSelected: Boolean, isMarker: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(if (isSelected) 36.dp else 30.dp) // Aumenta o tamanho se selecionado [cite: 2026-02-23]
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.White else color.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .padding(if (isSelected) 2.dp else 0.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isMarker) color.copy(alpha = 0.3f) else color, CircleShape)
                .then(if (isMarker) Modifier.border(1.dp, color, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (color == Color.White || isMarker) Color.White else Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Função crucial para o Rich Text: compara os códigos das cores, não os objetos [cite: 2026-02-23]
fun isColorEqual(colorInList: Color, selectedColor: Color, defaultMatch: Color): Boolean {
    val selectedArgb = selectedColor.toArgb()
    val listArgb = colorInList.toArgb()

    // Se bater o ARGB, está selecionado [cite: 2026-02-23]
    if (selectedArgb == listArgb) return true

    // Fallback: se o Rich Text retornar "transparente" ou "indefinido", marca o Branco por padrão [cite: 2026-02-23]
    if (colorInList == defaultMatch && (selectedArgb == 0 || selectedColor == Color.Unspecified)) return true

    return false
}