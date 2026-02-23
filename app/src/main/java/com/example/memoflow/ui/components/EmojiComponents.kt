package com.example.memoflow.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmojiSelector(
    emojis: List<Pair<String, String>>,
    selectedEmoji: String, // Este valor vem do uiState.selectedEmoji
    onEmojiSelected: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Como está o seu humor hoje?",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(emojis) { (emoji, humor) ->
                // AQUI ESTÁ O SEGREDO: Comparar o emoji da lista com o selecionado
                val isSelected = emoji == selectedEmoji

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEmojiSelected(emoji, humor) }
                        .padding(8.dp)
                ) {
                    Text(
                        text = emoji,
                        // RESPONSIVIDADE: Se for o selecionado, fica grande (42), se não, fica normal (28)
                        fontSize = if (isSelected) 42.sp else 28.sp
                    )

                    // Mostra o texto do humor apenas para o que você clicou
                    if (isSelected) {
                        Text(
                            text = humor,
                            color = Color(0xFF00FFC2), // Seu Neon Green
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}