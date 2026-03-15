package com.example.memoflow.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memoflow.ui.components.home.rememberAnimatedAiGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onBack: () -> Unit
) {
    val animatedGradient = rememberAnimatedAiGradient()
    var allEnabled by remember { mutableStateOf(true) }
    
    // Estados das notificações individuais
    var dailyReminder by remember { mutableStateOf(true) }
    var timeCapsule by remember { mutableStateOf(true) }
    var lockedNotes by remember { mutableStateOf(false) }
    var gratitudeReminder by remember { mutableStateOf(true) }
    
    // Novas Preferências de Som
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Central de Notificações", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Interruptor Mestre - O gradiente só aparece se estiver ativado
                NotificationCard(
                    title = "Ativar Notificações",
                    subtitle = "Habilitar todas as comunicações do Memo Flow",
                    icon = if (allEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    isChecked = allEnabled,
                    onCheckedChange = { allEnabled = it },
                    isMaster = true,
                    gradient = if (allEnabled) animatedGradient else null
                )
            }

            item {
                Text(
                    "CATEGORIAS",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, start = 8.dp)
                )
            }

            item {
                AnimatedVisibility(visible = allEnabled, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        NotificationCard(
                            title = "Momento Flow",
                            subtitle = "Lembrete diário para registrar seu dia",
                            icon = Icons.Default.AutoAwesome,
                            isChecked = dailyReminder,
                            onCheckedChange = { dailyReminder = it }
                        )
                        NotificationCard(
                            title = "Descongelamento",
                            subtitle = "Avisa quando uma cápsula do tempo abrir",
                            icon = Icons.Default.AcUnit,
                            isChecked = timeCapsule,
                            onCheckedChange = { timeCapsule = it }
                        )
                        NotificationCard(
                            title = "Ecos do Passado",
                            subtitle = "Sugestões para rever notas trancadas",
                            icon = Icons.Default.History,
                            isChecked = lockedNotes,
                            onCheckedChange = { lockedNotes = it }
                        )
                        NotificationCard(
                            title = "Pote de Gratidão",
                            subtitle = "Incentivo quando o pote estiver vazio",
                            icon = Icons.Default.Favorite,
                            isChecked = gratitudeReminder,
                            onCheckedChange = { gratitudeReminder = it }
                        )

                        Text(
                            "PREFERÊNCIAS DE ALERTA",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, start = 8.dp)
                        )

                        NotificationCard(
                            title = "Alertas Sonoros",
                            subtitle = "Tocar som ao receber notificações",
                            icon = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            isChecked = soundEnabled,
                            onCheckedChange = { soundEnabled = it }
                        )
                        NotificationCard(
                            title = "Vibração",
                            subtitle = "Vibrar ao receber alertas",
                            icon = Icons.Default.Vibration,
                            isChecked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    "O Memo Flow respeita sua paz. Notificações inteligentes são enviadas apenas em horários de baixa atividade.",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun NotificationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isMaster: Boolean = false,
    gradient: androidx.compose.ui.graphics.Brush? = null
) {
    val baseModifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF121212), RoundedCornerShape(24.dp))
    
    val modifierWithBorder = if (isMaster) {
        if (gradient != null) {
            baseModifier.border(1.5.dp, gradient, RoundedCornerShape(24.dp))
        } else {
            baseModifier.border(1.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
        }
    } else {
        baseModifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    }

    Row(
        modifier = modifierWithBorder.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isChecked) Color(0xFF00FFC2).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                    androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, 
                null, 
                tint = if (isChecked) Color(0xFF00FFC2) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, 
                color = Color.White, 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle, 
                color = Color.Gray, 
                fontSize = 12.sp, 
                lineHeight = 16.sp
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00FFC2),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF1A1A1A)
            )
        )
    }
}
