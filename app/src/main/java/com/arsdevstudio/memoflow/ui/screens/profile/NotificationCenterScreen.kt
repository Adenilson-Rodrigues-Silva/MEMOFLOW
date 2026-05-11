package com.arsdevstudio.memoflow.ui.screens.profile

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.arsdevstudio.memoflow.R
import com.arsdevstudio.memoflow.ui.components.home.rememberAnimatedAiGradient
import com.arsdevstudio.memoflow.utils.NotificationPrefs

@Composable
fun rememberGalaxyAiGradient(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "galaxy_gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "offset"
    )
    
    return Brush.linearGradient(
        colors = listOf(Color(0xFF6A1B9A), Color(0xFFFF4081), Color(0xFF00E5FF), Color(0xFF6A1B9A)),
        start = Offset(offset, offset),
        end = Offset(offset + 1000f, offset + 1000f),
        tileMode = TileMode.Repeated
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val context = LocalContext.current
    val settings by viewModel.notificationSettings.collectAsState()
    
    val animatedGradient = rememberAnimatedAiGradient()
    val galaxyGradient = rememberGalaxyAiGradient()
    
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = settings?.dailyHour ?: 20, 
        initialMinute = settings?.dailyMinute ?: 0, 
        is24Hour = true
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            viewModel.updateNotificationToggle(NotificationPrefs.ALL_ENABLED, false)
            Toast.makeText(context, context.getString(R.string.notif_permission_required), Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.notif_center_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        if (settings == null) return@Scaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                NotificationCard(
                    title = stringResource(R.string.notif_all_enabled),
                    subtitle = stringResource(R.string.notif_all_enabled_desc),
                    icon = if (settings!!.allEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    isChecked = settings!!.allEnabled,
                    onCheckedChange = { 
                        viewModel.updateNotificationToggle(NotificationPrefs.ALL_ENABLED, it)
                        if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    isMaster = true,
                    gradient = if (settings!!.allEnabled) animatedGradient else null
                )
            }

            item {
                Text(stringResource(R.string.notif_special_events), color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, start = 8.dp))
            }

            item {
                AnimatedVisibility(visible = settings!!.allEnabled, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    NotificationCard(
                        title = stringResource(R.string.notif_new_year_title),
                        subtitle = stringResource(R.string.notif_new_year_desc),
                        icon = Icons.Default.AutoGraph,
                        isChecked = settings!!.newYearEnabled,
                        onCheckedChange = { viewModel.updateNotificationToggle(NotificationPrefs.NEW_YEAR_ENABLED, it) },
                        isMaster = true,
                        gradient = if (settings!!.newYearEnabled) galaxyGradient else null
                    )
                }
            }

            item {
                Text(stringResource(R.string.notif_smart_categories), color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, start = 8.dp))
            }

            item {
                AnimatedVisibility(visible = settings!!.allEnabled, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            NotificationCard(
                                title = stringResource(R.string.notif_moment_flow_title),
                                subtitle = stringResource(R.string.notif_moment_flow_desc),
                                icon = Icons.Default.AutoAwesome,
                                isChecked = settings!!.dailyEnabled,
                                onCheckedChange = { viewModel.updateNotificationToggle(NotificationPrefs.DAILY_ENABLED, it) }
                            )
                            AnimatedVisibility(visible = settings!!.dailyEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 12.dp, end = 12.dp).clickable { showTimePicker = true }.background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp)).padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(stringResource(R.string.notif_reminder_time), color = Color.Gray, fontSize = 14.sp)
                                    Text(
                                        "${settings!!.dailyHour.toString().padStart(2, '0')}:${settings!!.dailyMinute.toString().padStart(2, '0')}", 
                                        color = Color(0xFF00FFC2), 
                                        fontWeight = FontWeight.Bold, 
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        NotificationCard(title = stringResource(R.string.notif_defrost_title), subtitle = stringResource(R.string.notif_defrost_desc), icon = Icons.Default.AcUnit, isChecked = settings!!.capsuleEnabled, onCheckedChange = { viewModel.updateNotificationToggle(NotificationPrefs.CAPSULE_ENABLED, it) })
                        NotificationCard(title = stringResource(R.string.notif_gratitude_pot_title), subtitle = stringResource(R.string.notif_gratitude_pot_desc), icon = Icons.Default.Favorite, isChecked = settings!!.gratitudeEnabled, onCheckedChange = { viewModel.updateNotificationToggle(NotificationPrefs.GRATITUDE_ENABLED, it) })
                        NotificationCard(title = stringResource(R.string.notif_past_echoes_title), subtitle = stringResource(R.string.notif_past_echoes_desc), icon = Icons.Default.History, isChecked = settings!!.echoEnabled, onCheckedChange = { viewModel.updateNotificationToggle(NotificationPrefs.ECHO_ENABLED, it) })
                        NotificationCard(title = stringResource(R.string.notif_weekly_insight_title), subtitle = stringResource(R.string.notif_weekly_insight_desc), icon = Icons.Default.BarChart, isChecked = settings!!.insightEnabled, onCheckedChange = { viewModel.updateNotificationToggle(NotificationPrefs.INSIGHT_ENABLED, it) })

                        Text(stringResource(R.string.notif_alert_prefs), color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, start = 8.dp))

                        NotificationCard(title = stringResource(R.string.notif_sound_title), subtitle = stringResource(R.string.notif_sound_desc), icon = if (settings!!.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff, isChecked = settings!!.soundEnabled, onCheckedChange = { viewModel.updateNotificationToggle(NotificationPrefs.SOUND_ENABLED, it) })
                        NotificationCard(title = stringResource(R.string.notif_vibration_title), subtitle = stringResource(R.string.notif_vibration_desc), icon = Icons.Default.Vibration, isChecked = settings!!.vibrationEnabled, onCheckedChange = { viewModel.updateNotificationToggle(NotificationPrefs.VIBRATION_ENABLED, it) })
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text(stringResource(R.string.notif_footer), color = Color.Gray.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                containerColor = Color(0xFF1A1A1A),
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateReminderTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }) { Text(stringResource(R.string.notif_picker_confirm), color = Color(0xFF00FFC2)) }
                },
                dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.notif_picker_cancel), color = Color.White) } },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.notif_picker_title), color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                        TimePicker(state = timePickerState, colors = TimePickerDefaults.colors(selectorColor = Color(0xFF00FFC2), containerColor = Color(0xFF1A1A1A), periodSelectorSelectedContainerColor = Color(0xFF00FFC2)))
                    }
                }
            )
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
    val baseModifier = Modifier.fillMaxWidth().background(Color(0xFF121212), RoundedCornerShape(24.dp))
    val modifierWithBorder = if (isMaster) {
        if (gradient != null) baseModifier.border(1.5.dp, gradient, RoundedCornerShape(24.dp))
        else baseModifier.border(1.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
    } else {
        baseModifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    }

    Row(modifier = modifierWithBorder.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).background(if (isChecked) (if (isMaster && title == stringResource(R.string.notif_new_year_title)) Color(0xFF6A1B9A) else Color(0xFF00FFC2)).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (isChecked) (if (isMaster && title == stringResource(R.string.notif_new_year_title)) Color(0xFFFF4081) else Color(0xFF00FFC2)) else Color.Gray, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = if (title == stringResource(R.string.notif_new_year_title)) Color(0xFF6A1B9A) else Color(0xFF00FFC2), uncheckedThumbColor = Color.Gray, uncheckedTrackColor = Color(0xFF1A1A1A)))
    }
}

