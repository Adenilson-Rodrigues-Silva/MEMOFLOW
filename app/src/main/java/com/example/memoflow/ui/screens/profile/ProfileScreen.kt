package com.example.memoflow.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSecurityClick: () -> Unit,
    onNotificationsClick: () -> Unit, // Nova navegação para central de notificações
    onBackupClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditBioDialog by remember { mutableStateOf(false) }

    val neonGreen = Color(0xFF00FFC2)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> 
        uri?.let { viewModel.updateUserPhoto(context, it) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text("Configurações", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(120.dp).clip(CircleShape).clickable { galleryLauncher.launch("image/*") },
                            shape = CircleShape,
                            color = neonGreen
                        ) {
                            if (userSettings.profilePhotoUri == null) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(25.dp), tint = Color.Black)
                            } else {
                                AsyncImage(
                                    model = userSettings.profilePhotoUri, 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize().clip(CircleShape), 
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        SmallFloatingActionButton(
                            onClick = { galleryLauncher.launch("image/*") }, 
                            containerColor = Color.White, 
                            shape = CircleShape, 
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (userSettings.userName.isEmpty()) "Dono do Chronos" else userSettings.userName, 
                        color = Color.White, 
                        fontSize = 22.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showEditNameDialog = true }
                    )
                    Text(
                        text = if (userSettings.bio.isEmpty()) "Toque para adicionar uma bio" else userSettings.bio, 
                        color = Color.Gray, 
                        fontSize = 14.sp, 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center, 
                        modifier = Modifier.padding(vertical = 8.dp).clickable { showEditBioDialog = true }
                    )
                    Text("membro desde fev 2026", color = neonGreen.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                item {
                    SectionTitle("Privacidade")
                    ProfileOptionItem(title = "Segurança", subtitle = "Biometria e PIN", icon = Icons.Default.Lock, onClick = onSecurityClick)
                }

                item {
                    SectionTitle("Comunicação")
                    ProfileOptionItem(
                        title = "Central de Notificações", 
                        subtitle = "Lembretes, Cápsulas e Gratidão", 
                        icon = Icons.Default.NotificationsActive, 
                        onClick = onNotificationsClick
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

        if (showEditNameDialog) {
            var nameText by remember { mutableStateOf(userSettings.userName) }
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Editar Nome", color = Color.White) },
                text = {
                    TextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, 
                            unfocusedContainerColor = Color.Transparent, 
                            focusedTextColor = Color.White, 
                            unfocusedTextColor = Color.White,
                            cursorColor = neonGreen,
                            focusedIndicatorColor = neonGreen
                        )
                    )
                },
                confirmButton = { 
                    TextButton(onClick = { viewModel.updateUserName(nameText); showEditNameDialog = false }) { 
                        Text("SALVAR", color = neonGreen) 
                    } 
                },
                dismissButton = { 
                    TextButton(onClick = { showEditNameDialog = false }) { 
                        Text("CANCELAR", color = Color.White) 
                    } 
                }
            )
        }

        if (showEditBioDialog) {
            var bioText by remember { mutableStateOf(userSettings.bio) }
            AlertDialog(
                onDismissRequest = { showEditBioDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Editar Bio", color = Color.White) },
                text = {
                    TextField(
                        value = bioText,
                        onValueChange = { bioText = it },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, 
                            unfocusedContainerColor = Color.Transparent, 
                            focusedTextColor = Color.White, 
                            unfocusedTextColor = Color.White,
                            cursorColor = neonGreen,
                            focusedIndicatorColor = neonGreen
                        )
                    )
                },
                confirmButton = { 
                    TextButton(onClick = { viewModel.updateUserBio(bioText); showEditBioDialog = false }) { 
                        Text("SALVAR", color = neonGreen) 
                    } 
                },
                dismissButton = { 
                    TextButton(onClick = { showEditBioDialog = false }) { 
                        Text("CANCELAR", color = Color.White) 
                    } 
                }
            )
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
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, top = 16.dp)
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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(title, color = color, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }

        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}
