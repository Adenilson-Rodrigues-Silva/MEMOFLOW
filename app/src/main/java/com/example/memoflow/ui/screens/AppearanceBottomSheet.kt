package com.example.memoflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceBottomSheet(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit // Função que avisa qual cor foi escolhida
) {
    // Lista de cores Neon disponíveis para o Chronos
    val colorOptions = listOf(
        Color(0xFF00FFC2), // Neon Green (Padrão)
        Color(0xFFBB86FC), // Neon Purple
        Color(0xFF03DAC6), // Neon Teal
        Color(0xFFCF6679)  // Neon Pink/Red
    )

    // O componente que cria o efeito de "menu que sobe"
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212), // Fundo grafite escuro
        scrimColor = Color.Black.copy(alpha = 0.6f) // Escurece o fundo do app
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Personalizar Aparência",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Escolha a cor de destaque do seu diário:",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SELETOR DE CORES EM LINHA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colorOptions.forEach { color ->
                    // Cada círculo colorido é um botão
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                onColorSelected(color) // Avisa a cor escolhida
                                onDismiss() // Fecha o menu
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp)) // Espaço para não ficar colado embaixo
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppearanceBottomSheetPreview() {
    // Simulamos o menu aberto com uma cor padrão
    AppearanceBottomSheet(
        onDismiss = { },
        onColorSelected = { }
    )
}