package com.arsdevstudio.memoflow.ui.components



import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

@Composable
fun PhotoGrid(
    images: List<Uri>,
    onRemove: (Uri) -> Unit,
    onExpand: (Uri) -> Unit
) {
    // Estado para controlar o alerta de exclusão
    var imageToDelete by remember { mutableStateOf<Uri?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until 3) {
            val imageUri = images.getOrNull(i)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
                    .pointerInput(imageUri) {
                        detectTapGestures(
                            onTap = { imageUri?.let { onExpand(it) } },
                            onLongPress = {
                                // Em vez de excluir direto, abre o alerta [cite: 2026-02-08]
                                imageUri?.let { imageToDelete = it }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Add, null, tint = Color.Gray.copy(alpha = 0.5f))
                }
            }
        }
    }

    // Caixa de Diálogo de Confirmação [cite: 2026-02-08]
    if (imageToDelete != null) {
        AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text("Excluir Imagem?") },
            text = { Text("Deseja remover esta foto do seu diário?") },
            confirmButton = {
                TextButton(onClick = {
                    imageToDelete?.let { onRemove(it) }
                    imageToDelete = null
                }) {
                    Text("Excluir", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { imageToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
