package com.example.memoflow.ui.components.writenote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceBottomSheet(
    selectedFontFamily: FontFamily,
    onFontSelected: (FontFamily) -> Unit,
    onDismiss: () -> Unit,
    neonGreen: Color = Color(0xFF00FFC2)
) {
    val fonts = listOf(
        FontOption("Padrão", FontFamily.Default),
        FontOption("Máquina de Escrever", FontFamily.Monospace),
        FontOption("Manuscrita (Elegante)", FontFamily.Serif),
        FontOption("Estilo Cômico (Divertida)", FontFamily.Cursive) // Lembra a Comic Sans no Android
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        scrimColor = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Escolha o Estilo da Memória",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            fonts.forEach { option ->
                val isSelected = option.fontFamily == selectedFontFamily
                
                Surface(
                    onClick = { onFontSelected(option.fontFamily) },
                    color = if (isSelected) neonGreen.copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option.name,
                            color = if (isSelected) neonGreen else Color.White,
                            fontFamily = option.fontFamily,
                            fontSize = 17.sp
                        )
                        
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = neonGreen)
                        }
                    }
                }
            }
        }
    }
}

data class FontOption(val name: String, val fontFamily: FontFamily)
