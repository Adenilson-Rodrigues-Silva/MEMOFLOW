package com.arsdevstudio.memoflow.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.arsdevstudio.memoflow.R
import com.arsdevstudio.memoflow.ui.components.home.PurpleAI
import com.arsdevstudio.memoflow.ui.components.home.rememberAnimatedAiGradient
import com.arsdevstudio.memoflow.ui.viewmodel.AuthEvent
import com.arsdevstudio.memoflow.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSecurityClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onBackupClick: () -> Unit,
    onStoreClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val animatedGradient = rememberAnimatedAiGradient()
    
    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            if (event is AuthEvent.LogoutSuccess) {
                onLogoutSuccess()
            }
        }
    }
    val googleGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4285F4), 
            Color(0xFFEA4335), 
            Color(0xFFFBBC05), 
            Color(0xFF34A853)
        )
    )

    val infiniteTransition = rememberInfiniteTransition(label = "profile_shimmer")
    val shimmerValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )
    val silverShimmer = Brush.linearGradient(
        colors = listOf(Color(0xFF424242), Color(0xFFBDBDBD), Color.White, Color(0xFFE0E0E0), Color(0xFF424242)),
        start = Offset(shimmerValue, shimmerValue),
        end = Offset(shimmerValue + 400f, shimmerValue + 400f),
        tileMode = TileMode.Repeated
    )

    val premiumBrush = Brush.linearGradient(
        colors = listOf(PurpleAI, Color(0xFF6200EE), PurpleAI),
        start = Offset(shimmerValue, shimmerValue),
        end = Offset(shimmerValue + 500f, shimmerValue + 500f),
        tileMode = TileMode.Repeated
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditBioDialog by remember { mutableStateOf(false) }

    val neonGreen = Color(0xFF00FFC2)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.updateUserPhoto(context, it) } }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Text("Dono do Flow", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // CARD DE IDENTIDADE (FOTO E NOME)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, if (isPremium) premiumBrush else animatedGradient, RoundedCornerShape(32.dp)),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 40.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Surface(
                                    modifier = Modifier.size(140.dp).clip(CircleShape).clickable { galleryLauncher.launch("image/*") },
                                    shape = CircleShape,
                                    color = Color(0xFF1A1A1A),
                                    border = BorderStroke(2.5.dp, if (isPremium) premiumBrush else animatedGradient)
                                ) {
                                    if (userSettings?.profilePhotoUri == null) {
                                        Icon(Icons.Default.Person, null, modifier = Modifier.padding(35.dp), tint = Color.DarkGray)
                                    } else {
                                        AsyncImage(
                                            model = userSettings!!.profilePhotoUri, 
                                            contentDescription = null, 
                                            modifier = Modifier.fillMaxSize().clip(CircleShape), 
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier.size(38.dp).background(Color.White, CircleShape).border(1.dp, Color.Black, CircleShape).clickable { galleryLauncher.launch("image/*") }.padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.CameraAlt, null, tint = Color.Black, modifier = Modifier.size(18.dp)) }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (userSettings?.userName.isNullOrEmpty()) "Dono do Flow" else userSettings!!.userName, 
                                    color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.clickable { showEditNameDialog = true },
                                    textAlign = TextAlign.Center
                                )
                                if (isPremium) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.Star, "Premium", tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                                } else {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4285F4), modifier = Modifier.size(20.dp))
                                }
                            }
                            
                            if (userSettings?.email != null) {
                                Text(userSettings!!.email!!, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                            }

                            Text(
                                text = if (userSettings?.bio.isNullOrEmpty()) "Escreva algo sobre sua jornada..." else userSettings!!.bio, 
                                color = Color.Gray, fontSize = 15.sp, textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 10.dp, bottom = 20.dp).clickable { showEditBioDialog = true }
                            )
                            
                            Surface(
                                color = if (isPremium) PurpleAI.copy(alpha = 0.2f) else Color(0xFF6A00FF).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    Icon(
                                        if (isPremium) Icons.Default.Verified else Icons.Default.CloudDone, 
                                        null, 
                                        tint = if (isPremium) PurpleAI else Color(0xFFBB86FC), 
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (isPremium) "MEMBRO PREMIUM" else "Conta Sincronizada", 
                                        color = if (isPremium) PurpleAI else Color(0xFFBB86FC), fontSize = 11.sp, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

    // REMOVIDO: BOTÃO DE LOGIN GOOGLE (Agora o login é obrigatório para chegar aqui)

                // DASHBOARD DE AÇÕES
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProfileDashboardCard(
                            title = "Loja de Evolução", 
                            subtitle = if (isPremium) "Você é Premium! Ver doações" else "Adquira o Premium ou apoie o dev", 
                            icon = Icons.Default.ShoppingCart, 
                            gradient = if (isPremium) premiumBrush else animatedGradient, 
                            onClick = onStoreClick
                        )
                        ProfileDashboardCard(title = "Segurança Ativa", subtitle = "Gerencie seu PIN e Biometria", icon = Icons.Default.Shield, gradient = animatedGradient, onClick = onSecurityClick)
                        ProfileDashboardCard(title = "Fluxo de Alertas", subtitle = "Central de Notificações Inteligente", icon = Icons.Default.NotificationsActive, gradient = animatedGradient, onClick = onNotificationsClick)
                        ProfileDashboardCard(title = "Arquivos & Backup", subtitle = "Nuvem e Exportação de Memórias", icon = Icons.Default.AutoMode, gradient = animatedGradient, onClick = onBackupClick)
                    }
                }

                // BOTÃO DE LOGOUT
                item {
                    OutlinedButton(
                        onClick = { authViewModel.signOut() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SAIR DA CONTA GOOGLE", color = Color.Red.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                item {
                    TextButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp)) {
                        Icon(Icons.Default.DeleteSweep, null, tint = Color.Red.copy(alpha = 0.5f))
                        Spacer(Modifier.width(8.dp))
                        Text("RESETAR TODA A MINHA JORNADA", color = Color.Red.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // --- DIALOGS PRESERVADOS ---
        if (showEditNameDialog) {
            var nameText by remember { mutableStateOf(userSettings?.userName ?: "") }
            AlertDialog(onDismissRequest = { showEditNameDialog = false }, containerColor = Color(0xFF1A1A1A), title = { Text("Como quer ser chamado?", color = Color.White) },
                text = { OutlinedTextField(value = nameText, onValueChange = { nameText = it }, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = neonGreen, focusedBorderColor = neonGreen, unfocusedBorderColor = Color.DarkGray)) },
                confirmButton = { TextButton(onClick = { viewModel.updateUserName(nameText); showEditNameDialog = false }) { Text("SALVAR", color = neonGreen) } }
            )
        }

        if (showEditBioDialog) {
            var bioText by remember { mutableStateOf(userSettings?.bio ?: "") }
            AlertDialog(onDismissRequest = { showEditBioDialog = false }, title = { Text("Sua Biografia", color = Color.White) },
                text = { OutlinedTextField(value = bioText, onValueChange = { bioText = it }, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = neonGreen, focusedBorderColor = neonGreen, unfocusedBorderColor = Color.DarkGray)) },
                confirmButton = { TextButton(onClick = { viewModel.updateUserBio(bioText); showEditBioDialog = false }) { Text("SALVAR", color = neonGreen) } },
                containerColor = Color(0xFF1A1A1A)
            )
        }

        if (showDeleteDialog) {
            AlertDialog(onDismissRequest = { showDeleteDialog = false }, containerColor = Color(0xFF1A1A1A), title = { Text("Apagar tudo?", color = Color.White) },
                text = { Text("Esta ação removerá permanentemente todos os seus registros do dispositivo.", color = Color.Gray) },
                confirmButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("APAGAR TUDO", color = Color.Red) } },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCELAR", color = Color.White) } }
            )
        }
    }
}

@Composable
fun ProfileDashboardCard(title: String, subtitle: String, icon: ImageVector, gradient: Brush, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }.border(1.2.dp, gradient, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.05f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.DarkGray)
        }
    }
}

