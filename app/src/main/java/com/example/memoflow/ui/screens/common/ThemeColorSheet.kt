package com.example.memoflow.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeColorSheet(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val colorOptions = listOf(
        Color(0xFF00FFC2),
        Color(0xFFBB86FC),
        Color(0xFF03DAC6),
        Color(0xFFCF6679)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Personalizar Cor", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                colorOptions.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                onColorSelected(color)
                                onDismiss()
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
