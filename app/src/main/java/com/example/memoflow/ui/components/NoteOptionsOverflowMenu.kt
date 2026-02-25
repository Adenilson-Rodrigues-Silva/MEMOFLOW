package com.example.memoflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteOptionsOverflowMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onShareClick: () -> Unit,
    onFontStyleClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLockClick: () -> Unit,
    onTimeCapsuleClick: () -> Unit,
    neonGreen: Color = Color(0xFF00FFC2)
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            surface = Color(0xFF1E1E1E) // Cor de fundo do menu igual à do print
        )
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                .padding(vertical = 4.dp)
        ) {
            MenuOption(
                icon = Icons.Default.Share,
                label = "Compartilhar Memória",
                color = neonGreen,
                onClick = {
                    onShareClick()
                    onDismissRequest()
                }
            )
            MenuOption(
                icon = Icons.Default.TextFields,
                label = "Estilo da Fonte",
                color = neonGreen,
                onClick = {
                    onFontStyleClick()
                    onDismissRequest()
                }
            )
            MenuOption(
                icon = Icons.Default.Info,
                label = "Detalhes da Memória",
                color = neonGreen,
                onClick = {
                    onDetailsClick()
                    onDismissRequest()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.Gray.copy(alpha = 0.2f))

            MenuOption(
                icon = Icons.Default.DeleteForever,
                label = "Apagar para Sempre",
                color = Color.Red.copy(alpha = 0.8f),
                onClick = {
                    onDeleteClick()
                    onDismissRequest()
                }
            )
            MenuOption(
                icon = Icons.Default.Lock,
                label = "Trancar Memórias",
                color = neonGreen,
                onClick = {
                    onLockClick()
                    onDismissRequest()
                }
            )
            MenuOption(
                icon = Icons.Default.History,
                label = "Cápsula do Tempo",
                color = neonGreen,
                onClick = {
                    onTimeCapsuleClick()
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun MenuOption(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                color = color,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(end = 8.dp)
            )
        },
        onClick = onClick
    )
}
