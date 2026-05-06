package com.arsdevstudio.memoflow.ui.components.writenote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteDetailsDialog(
    noteId: String = "---", // Registro (futuro ID do banco)
    wordCount: Int,
    charCount: Int,
    hasAudio: Boolean,
    imageCount: Int,
    date: String,
    onDismiss: () -> Unit,
    neonGreen: Color = Color(0xFF00FFC2)
) {
    // Cálculo de leitura estimada (média de 200 palavras por minuto)
    val readingTime = if (wordCount < 200) "Menos de 1 min" else "${wordCount / 200} min"
    
    val statusText = if (imageCount > 0) "Texto + Imagens" else "Apenas Texto"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = neonGreen)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Estatísticas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailItem("📝", "Registro", noteId, neonGreen)
                DetailItem("📅", "Data", date, neonGreen)
                DetailItem("🖼️", "Status", statusText, neonGreen)
                DetailItem("🔢", "Caracteres", "$charCount", neonGreen)
                DetailItem("✍️", "Palavras", "$wordCount", neonGreen)
                DetailItem("⏱️", "Leitura", readingTime, neonGreen)
                
                if (hasAudio) {
                    DetailItem("🎤", "Áudio", "Anexado", neonGreen)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entendido!", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DetailItem(emoji: String, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = Color.Gray, fontSize = 14.sp)
        }
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

