package com.arsdevstudio.memoflow.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.arsdevstudio.memoflow.ui.viewmodel.WriteNoteViewModel

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

@Composable
fun AudioPlayerComponent(
    accentColor: Color,
    isRecording: Boolean,
    isPlaying: Boolean,
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
                        isPlaying -> accentColor
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

            // ✅ Correção: Usando o parâmetro 'progress' como lambda se necessário ou valor simples conforme versão
            LinearProgressIndicator(
                progress = { currentTime / 30f },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = if (isRecording) Color.Red else accentColor,
                trackColor = Color.DarkGray
            )

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

