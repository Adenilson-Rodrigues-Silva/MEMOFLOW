package com.arsdevstudio.memoflow.ui.screens.profile

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.*
import com.arsdevstudio.memoflow.R
import com.arsdevstudio.memoflow.ui.components.home.rememberAnimatedAiGradient
import com.arsdevstudio.memoflow.utils.GoogleDriveService

/**
 * PONTO DE RETORNO (INÍCIO DO MÊS):
 * - Esta tela depende do estado 'isPremium' para liberar o Google Drive.
 * - Atualmente funciona com o modo de teste (clique longo no ícone da estrela na Loja).
 * - Quando a conta de desenvolvedor estiver ativa, o 'isPremium' virá automaticamente do BillingManager.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = viewModel(factory = BackupViewModel.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val animatedGradient = rememberAnimatedAiGradient()
    val driveService = remember { GoogleDriveService(context) }

    val drivePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Permissão concedida! Tente a ação novamente.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permissão do Drive negada pelo Google.", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importBackup(context, it) }
    }

    LaunchedEffect(uiState) {
        if (uiState is BackupViewModel.BackupUiState.Success) {
            Toast.makeText(context, (uiState as BackupViewModel.BackupUiState.Success).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        } else if (uiState is BackupViewModel.BackupUiState.Error) {
            Toast.makeText(context, (uiState as BackupViewModel.BackupUiState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Backup e Restauração", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, animatedGradient, RoundedCornerShape(24.dp))
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    if (isPremium) {
                                        if (driveService.hasDrivePermission()) {
                                            viewModel.uploadToDriveManual(context)
                                        } else {
                                            drivePermissionLauncher.launch(driveService.getGoogleSignInClient().signInIntent)
                                        }
                                    }
                                }
                            )
                        }
                    ) {
                        Icon(Icons.Default.CloudQueue, null, tint = if (isPremium) Color(0xFFFFD700) else Color(0xFF00FFC2))
                        Spacer(Modifier.width(8.dp))
                        Text("Backup na Nuvem", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (isPremium) "Sincronização automática ativa.\n(Segure aqui para enviar agora)" else "Assine o Premium para ativar a sincronização automática.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    if (isPremium) {
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { 
                                if (driveService.hasDrivePermission()) {
                                    viewModel.restoreFromDrive(context)
                                } else {
                                    drivePermissionLauncher.launch(driveService.getGoogleSignInClient().signInIntent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .background(animatedGradient, RoundedCornerShape(12.dp))
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text("RESTAURAR DA NUVEM", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "BACKUP MANUAL",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 12.dp)
            )

            BackupOptionCard(
                title = "Exportar Dados",
                subtitle = "Gera um arquivo .json do seu Flow",
                icon = Icons.Default.FileUpload,
                onClick = {
                    viewModel.exportBackup(context) { uri ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Salvar Backup"))
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BackupOptionCard(
                title = "Importar Dados",
                subtitle = "Restaura de um arquivo local",
                icon = Icons.Default.FileDownload,
                onClick = { importLauncher.launch("application/json") }
            )
        }
    }

    if (uiState is BackupViewModel.BackupUiState.DriveLoading) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(32.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_google_drive))
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(160.dp),
                        enableMergePaths = true,
                        renderMode = RenderMode.SOFTWARE // ✅ Força as cores
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Conectando com a nuvem...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }

    if (uiState is BackupViewModel.BackupUiState.Loading) {
        BackupLoadingDialog()
    }
}

@Composable
fun BackupLoadingDialog() {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(32.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_backup))
                val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
                
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(140.dp),
                    enableMergePaths = true,
                    renderMode = RenderMode.SOFTWARE
                )
                
                Spacer(Modifier.height(12.dp))
                Text(
                    "Processando dados...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BackupOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF121212),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.05f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

