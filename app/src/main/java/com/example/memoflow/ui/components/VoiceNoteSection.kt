package com.example.memoflow.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // Aqui já inclui o getValue e setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

// IMPORTANTE: Verifique se este é o caminho real do seu State e ViewModel
//import com.example.memoflow.ui.viewmodel.WriteNoteViewModel
//import com.example.memoflow.ui.screens.WriteNoteState
import com.example.memoflow.viewmodel.WriteNoteViewModel

@Composable
fun VoiceNoteSection(
    isRecording: Boolean,
    recordingTime: Int,
    audioPath: String?,
    viewModel: WriteNoteViewModel,
    context: Context,
    permissionLauncher: ActivityResultLauncher<String>,
    accentColor: Color
) {
    var showAudioDeleteDialog by remember { mutableStateOf(false) }

    if (showAudioDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showAudioDeleteDialog = false },
            title = { Text("Apagar Gravação?") },
            text = { Text("Deseja remover o áudio desta nota para gravar um novo?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAudio()
                    showAudioDeleteDialog = false
                }) { Text("Apagar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showAudioDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Só mostra o player se estiver gravando ou se já existir um áudio salvo
    if (isRecording || audioPath != null) {
        AudioPlayerComponent(
            accentColor = accentColor,
            isRecording = isRecording,
            isPlaying = viewModel.isPlaying,
            currentTime = recordingTime,
            audioPath = audioPath,
            onPlayClick = { viewModel.playAudio() },
            onDeleteClick = { showAudioDeleteDialog = true }
        )
    }
}

// Mova sua função @Composable AudioPlayerComponent para cá também!
@Composable
fun AudioPlayerComponent(
    accentColor: Color,
    isRecording: Boolean,
    isPlaying: Boolean, // A "chave" que adicionamos
    currentTime: Int,
    audioPath: String?,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BOTÃO DE PLAY/PAUSE/MIC
            IconButton(
                onClick = { onPlayClick() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when {
                        isRecording -> Icons.Default.Mic
                        isPlaying -> Icons.Default.Pause
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = "Play/Pause",
                    tint = when {
                        isRecording -> Color.Red
                        isPlaying -> accentColor // Fica Neon Green (0xFF00FFC2)
                        else -> Color.White
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            val timeText = if (currentTime < 10) "0:0$currentTime" else "0:$currentTime"
            Text(
                text = "$timeText / 0:30",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // BARRA DE PROGRESSO
            LinearProgressIndicator(
                progress = currentTime / 30f,
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = when {
                    isRecording -> Color.Red
                    isPlaying -> accentColor
                    else -> accentColor.copy(alpha = 0.5f)
                },
                trackColor = Color.DarkGray
            )

            // LIXEIRA (Só aparece se tiver áudio e não estiver gravando)
            if (audioPath != null && !isRecording) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Função utilitária para ser chamada no botão de Microfone da BottomBar
 */
fun handleVoiceClick(
    context: Context,
    isRecording: Boolean,
    audioPath: String?,
    viewModel: WriteNoteViewModel,
    permissionLauncher: ActivityResultLauncher<String>
) {
    if (isRecording) {
        viewModel.onVoiceClick(context.cacheDir)
    } else if (audioPath != null) {
        Toast.makeText(context, "Já existe um áudio. Apague para gravar outro.", Toast.LENGTH_SHORT).show()
    } else {
        val permission = android.Manifest.permission.RECORD_AUDIO
        val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            viewModel.onVoiceClick(context.cacheDir)
        } else {
            permissionLauncher.launch(permission)
        }
    }
}