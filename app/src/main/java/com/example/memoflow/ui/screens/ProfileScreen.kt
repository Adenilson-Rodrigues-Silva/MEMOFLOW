package com.example.memoflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSecurityClick: () -> Unit,
    onBackupClick: () -> Unit
) {
    // --- ESTADOS ---
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAppearanceSheet by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(initialHour = 20, initialMinute = 0, is24Hour = true)
    var reminderDisplayText by remember { mutableStateOf("Desativado") }
    var isReminderActive by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color(0xFF00FFC2)) }
    val neonGreen = Color(0xFF00FFC2)
    var userBio by remember { mutableStateOf("Escrevendo para não esquecer quem eu era.") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    // --- LAYOUT PRINCIPAL ---
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Toolbar
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text("Configurações", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    // Foto de Perfil
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(120.dp).clickable { galleryLauncher.launch("image/*") },
                            shape = CircleShape,
                            color = neonGreen
                        ) {
                            if (selectedImageUri == null) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(25.dp), tint = Color.Black)
                            } else {
                                AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                            }
                        }
                        SmallFloatingActionButton(onClick = { galleryLauncher.launch("image/*") }, containerColor = Color.White, shape = CircleShape, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dono do Chronos", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = userBio, color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                    Text("membro desde fev 2026", color = neonGreen.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                item {
                    SectionTitle("Privacidade")
                    ProfileOptionItem(title = "Segurança", subtitle = "Biometria e PIN", icon = Icons.Default.Lock, onClick = onSecurityClick)
                }

                item {
                    SectionTitle("Personalização")
                    ProfileOptionItem(title = "Aparência", subtitle = "Tema e Cores", icon = Icons.Default.Palette, onClick = { showAppearanceSheet = true })

                    // AQUI O SUBTITLE ATUALIZA
                    ProfileOptionItem(
                        title = "Lembrete Diário",
                        subtitle = if (isReminderActive) "Definido para $reminderDisplayText" else "Toque para ativar",
                        icon = Icons.Default.Notifications,
                        onClick = { showTimePicker = true }
                    )
                }

                item {
                    SectionTitle("Dados")
                    ProfileOptionItem(title = "Backup", subtitle = "Nuvem e Exportação PDF", icon = Icons.Default.CloudUpload, onClick = onBackupClick)
                    ProfileOptionItem(title = "Limpar Diário", subtitle = "Apagar todas as memórias", icon = Icons.Default.DeleteForever, color = Color.Red.copy(alpha = 0.8f), onClick = { showDeleteDialog = true })
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }

        // --- COMPONENTES OVERLAY (Dialogs e Sheets) ---

        if (showAppearanceSheet) {
            AppearanceBottomSheet(onDismiss = { showAppearanceSheet = false }, onColorSelected = { selectedColor = it; showAppearanceSheet = false })
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Apagar todas as memórias?", color = Color.White) },
                text = { Text("Esta ação é irreversível.", color = Color.Gray) },
                confirmButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("APAGAR TUDO", color = Color.Red) } },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCELAR", color = Color.White) } }
            )
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                containerColor = Color(0xFF1A1A1A),
                confirmButton = {
                    TextButton(onClick = {
                        // LÓGICA CORRETA NO LUGAR CERTO
                        val h = timePickerState.hour.toString().padStart(2, '0')
                        val m = timePickerState.minute.toString().padStart(2, '0')
                        reminderDisplayText = "$h:$m"
                        isReminderActive = true
                        showTimePicker = false
                    }) { Text("DEFINIR", color = neonGreen) }
                },
                dismissButton = {
                    TextButton(onClick = { isReminderActive = false; showTimePicker = false }) { Text("REMOVER", color = Color.Red) }
                },
                text = {
                    Column {
                        Text("Horário do lembrete", color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                        TimePicker(state = timePickerState, colors = TimePickerDefaults.colors(selectorColor = neonGreen, containerColor = Color(0xFF1A1A1A)))
                    }
                }
            )
        }
    }
}

// COMPONENTES AUXILIARES (Sem alteração)

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 16.dp)
    )
}


@Composable
fun ProfileOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícone com fundo sutil
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(title, color = color, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight, // Use o nome oficial direto aqui
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}


@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun ProfileScreenPreview() {
    // Usamos o tema do seu projeto para as cores ficarem certas
    com.example.memoflow.ui.theme.MemoFlowTheme(darkTheme = true) {
        ProfileScreen(
            onBack = { },
            onSecurityClick = { },
            onBackupClick = { })// <-- ADICIONE ISSO)

    }
}

