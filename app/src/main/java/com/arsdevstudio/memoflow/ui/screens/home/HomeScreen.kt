package com.arsdevstudio.memoflow.ui.screens.home

import android.util.Log
import android.widget.Toast
import com.arsdevstudio.memoflow.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.ui.components.home.*
import com.arsdevstudio.memoflow.ui.viewmodel.HomeViewModel
import com.arsdevstudio.memoflow.ui.screens.profile.BackupViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.arsdevstudio.memoflow.navigation.Screen
import java.time.Instant
import java.time.ZoneId
import com.arsdevstudio.memoflow.ui.screens.profile.ProfileViewModel
import com.arsdevstudio.memoflow.ui.screens.security.SecurityViewModel
import com.arsdevstudio.memoflow.ui.screens.stats.StatisticsViewModel
import com.arsdevstudio.memoflow.ui.screens.common.ChronosCalendarSheet
import com.arsdevstudio.memoflow.ui.viewmodel.WriteNoteViewModel
import com.arsdevstudio.memoflow.ui.screens.recall.RecallViewModel
import com.arsdevstudio.memoflow.ui.viewmodel.GratitudeViewModel
import com.arsdevstudio.memoflow.ui.viewmodel.NotificationViewModel
import com.arsdevstudio.memoflow.data.local.entity.NotificationEntity
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    activity: FragmentActivity,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
    securityViewModel: SecurityViewModel = viewModel(factory = SecurityViewModel.Factory),
    statsViewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory),
    writeNoteViewModel: WriteNoteViewModel = viewModel(factory = WriteNoteViewModel.Factory),
    recallViewModel: RecallViewModel = viewModel(factory = RecallViewModel.Factory),
    notificationViewModel: NotificationViewModel = viewModel(factory = NotificationViewModel.Factory)
) {
    val context = LocalContext.current
    val neonGreen = Color(0xFF00FFC2)
    val iceBlue = Color(0xFF80DEEA)
    
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showChronosCalendar by remember { mutableStateOf(false) }
    var showStatusSheet by remember { mutableStateOf(false) }
    
    val notes by viewModel.notes.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val markedDates by viewModel.markedDates.collectAsState()
    val userSettings by profileViewModel.userSettings.collectAsState()
    val isPremium by profileViewModel.isPremium.collectAsState()
    val securitySettings by securityViewModel.userSettings.collectAsState()
    val statsData by statsViewModel.statsData.collectAsState()
    var showNotificationsSheet by remember { mutableStateOf(false) }
    
    val notifications by notificationViewModel.notifications.collectAsState(initial = emptyList())
    val unreadCount by notificationViewModel.unreadCount.collectAsState(initial = 0)

    val backupViewModel: BackupViewModel = viewModel(factory = BackupViewModel.Factory)

    // Biometria removida daqui para não pedir toda hora ao navegar


    LaunchedEffect(Unit) {
        backupViewModel.silentRestore(context)
    }
    
    // Re-checar notificações ao focar ou voltar para a Home
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationViewModel.triggerCheck()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val remainingRecalls by recallViewModel.remainingRefreshes.collectAsState()
    val maxRecalls = if (isPremium) 6 else 2
    
    // Se não estiver logado, manda para a WelcomeAuth imediatamente
    LaunchedEffect(userSettings) {
        // Aguarda carregar o estado (null -> carregado)
        if (userSettings != null) {
            if (!userSettings!!.isGoogleLogged) {
                navController.navigate(Screen.WelcomeAuth.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val userId = userSettings?.firebaseUid ?: ""
    var totalCapsules by remember { mutableIntStateOf(0) }
    LaunchedEffect(notes, isPremium, userId) {
        if (userId.isNotEmpty()) {
            val app = context.applicationContext as com.arsdevstudio.memoflow.MemoApplication
            totalCapsules = app.repository.getTimeCapsuleCount(userId)
        }
    }

    var noteToUnlock by remember { mutableStateOf<NoteEntity?>(null) }
    var showMeltOptions by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    
    val isToday = remember(selectedDate) { selectedDate == LocalDate.now() }

    LaunchedEffect(selectedDate) {
        statsViewModel.setReferenceDate(selectedDate)
    }

    val rotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 45f else 0f,
        label = "fab_rotation"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                if (isToday) {
                    FabMenu(
                        navController = navController,
                        isMenuExpanded = isMenuExpanded,
                        rotation = rotation,
                        neonGreen = neonGreen,
                        onToggle = { isMenuExpanded = !isMenuExpanded },
                        canAddNote = viewModel.canAddNote(isPremium),
                        onLimitReached = {
                            Toast.makeText(context, context.getString(R.string.home_limit_reached), Toast.LENGTH_SHORT).show()
                        },
                        isToday = isToday
                    )
                }
            }
        ) { padding ->
            HomeContent(
                padding = padding, 
                neonGreen = neonGreen, 
                navController = navController,
                notes = notes,
                selectedDate = selectedDate,
                onDateSelected = { viewModel.onDateSelected(it) },
                onDeleteNote = { viewModel.deleteNote(it) },
                userPhotoUrl = userSettings?.profilePhotoUri,
                moodPoints = statsData.moodPoints,
                onCalendarClick = { showChronosCalendar = true },
                onNotificationsClick = { showNotificationsSheet = true },
                onStatusClick = { showStatusSheet = true },
                isPremium = isPremium,
                unreadNotifications = unreadCount,
                onNoteClick = { note ->
                    val noteDate = Instant.ofEpochMilli(note.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    val isNoteFromToday = noteDate == LocalDate.now()
                    
                    if (note.isLocked || note.isTimeCapsule) {
                        noteToUnlock = note
                    } else {
                        val route = Screen.WriteNote.createRoute(note.id) + if (!isNoteFromToday) "&readOnly=true" else ""
                        navController.navigate(route)
                    }
                }
            )
        }

        if (showNotificationsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showNotificationsSheet = false },
                containerColor = Color(0xFF121212),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
            ) {
                NotificationBottomSheetContent(
                    notifications = notifications,
                    onMarkAllAsRead = { notificationViewModel.markAllAsRead() },
                    onDeleteAll = { notificationViewModel.deleteAllNotifications() },
                    onDelete = { notificationViewModel.deleteNotification(it) },
                    onNotificationClick = { notification ->
                        notificationViewModel.markAsRead(notification)
                        showNotificationsSheet = false
                        when (notification.type) {
                            "CAPSULE" -> {
                                notification.targetId?.let { noteId ->
                                    navController.navigate(Screen.WriteNote.createRoute(noteId.toLong()) + "&readOnly=true")
                                }
                            }
                            "DONATION" -> {
                                navController.navigate(Screen.Store.route)
                            }
                            "INFO" -> {
                                if (notification.targetId == "STREAK_REMINDER") {
                                    navController.navigate(Screen.WriteNote.createRoute())
                                }
                            }
                        }
                    }
                )
            }
        }

        if (showStatusSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStatusSheet = false },
                containerColor = Color(0xFF121212),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
            ) {
                StatusInfoContent(
                    isPremium = isPremium,
                    noteCountToday = notes.size,
                    capsuleCount = totalCapsules,
                    remainingRecalls = remainingRecalls,
                    maxRecalls = maxRecalls,
                    onUpgradeClick = {
                        showStatusSheet = false
                        navController.navigate(Screen.Store.route)
                    }
                )
            }
        }


        if (showChronosCalendar) {
            ChronosCalendarSheet(
                onDismiss = { showChronosCalendar = false },
                selectedDate = selectedDate,
                markedDates = markedDates,
                onDateSelected = { 
                    viewModel.onDateSelected(it)
                    showChronosCalendar = false
                },
                neonGreen = neonGreen
            )
        }

        if (noteToUnlock != null && !showMeltOptions) {
            val isCapsule = noteToUnlock?.isTimeCapsule == true
            AlertDialog(
                onDismissRequest = { noteToUnlock = null; pinInput = "" },
                containerColor = Color(0xFF1A1A1A),
                title = { Text(if (isCapsule) stringResource(R.string.home_access_capsule) else stringResource(R.string.home_locked_memory), color = Color.White) },
                text = {
                    Column {
                        Text(stringResource(R.string.home_pin_instructions), color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 4) pinInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = neonGreen,
                                focusedIndicatorColor = neonGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (pinInput == securitySettings.pin) {
                            if (isCapsule) {
                                showMeltOptions = true 
                            } else {
                                val id = noteToUnlock?.id
                                val noteDate = noteToUnlock?.date?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                                val isNoteFromToday = noteDate == LocalDate.now()
                                
                                noteToUnlock = null
                                pinInput = ""
                                val route = Screen.WriteNote.createRoute(id) + if (!isNoteFromToday) "&readOnly=true" else ""
                                navController.navigate(route)
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.home_wrong_pin), Toast.LENGTH_SHORT).show()
                            pinInput = ""
                        }
                    }) {
                        Text(stringResource(R.string.home_verify), color = neonGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noteToUnlock = null; pinInput = "" }) {
                        Text(stringResource(R.string.home_cancel), color = Color.White)
                    }
                }
            )
        }

        if (showMeltOptions) {
            val isReady = remember(noteToUnlock) {
                noteToUnlock?.unlockDate?.let { unlockDate ->
                    val now = System.currentTimeMillis()
                    if (now >= unlockDate) true
                    else {
                        val unlockDay = Instant.ofEpochMilli(unlockDate)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                        val today = LocalDate.now()
                        !today.isBefore(unlockDay)
                    }
                } ?: false
            }
            AlertDialog(
                onDismissRequest = { showMeltOptions = false; noteToUnlock = null; pinInput = "" },
                containerColor = Color(0xFF1A1A1A),
                title = { Text(if(isReady) stringResource(R.string.home_ice_melted) else stringResource(R.string.home_still_frozen), color = Color.White) },
                text = { Text(if(isReady) stringResource(R.string.home_melt_ready_desc) else stringResource(R.string.home_melt_wait_desc), color = Color.Gray) },
                confirmButton = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (isReady) {
                            Button(
                                onClick = {
                                    noteToUnlock?.let { writeNoteViewModel.meltPermanently(it.id) }
                                    val id = noteToUnlock?.id
                                    val noteDate = noteToUnlock?.date?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                                    val isNoteFromToday = noteDate == LocalDate.now()
                                    
                                    showMeltOptions = false
                                    noteToUnlock = null
                                    pinInput = ""
                                    val route = Screen.WriteNote.createRoute(id) + if (!isNoteFromToday) "&readOnly=true" else ""
                                    navController.navigate(route)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = neonGreen)
                            ) {
                                Text(stringResource(R.string.home_melt_permanently), color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    val id = noteToUnlock?.id
                                    showMeltOptions = false
                                    noteToUnlock = null
                                    pinInput = ""
                                    navController.navigate(Screen.WriteNote.createRoute(id) + "&readOnly=true")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, iceBlue)
                            ) {
                                Text(stringResource(R.string.home_just_peek), color = iceBlue)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = { showMeltOptions = false; noteToUnlock = null; pinInput = "" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.home_cancel), color = Color.White)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun StatusInfoContent(
    isPremium: Boolean,
    noteCountToday: Int,
    capsuleCount: Int,
    remainingRecalls: Int,
    maxRecalls: Int,
    onUpgradeClick: () -> Unit
) {
    val neonGreen = Color(0xFF00FFC2)
    val purpleAI = Color(0xFFBB86FC)
    val gold = Color(0xFFFFD700)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            if (isPremium) Icons.Default.Verified else Icons.Default.Info,
            contentDescription = null,
            tint = if (isPremium) gold else neonGreen,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (isPremium) stringResource(R.string.home_premium_active) else stringResource(R.string.home_flow_status),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            if (isPremium) stringResource(R.string.home_premium_desc) else stringResource(R.string.home_free_desc),
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(32.dp))

        StatusItem(
            label = stringResource(R.string.home_notes_today),
            value = "$noteCountToday/3",
            progress = noteCountToday / 3f,
            color = neonGreen
        )

        Spacer(Modifier.height(20.dp))

        StatusItem(
            label = stringResource(R.string.home_time_capsules),
            value = if (isPremium) "$capsuleCount/∞" else "$capsuleCount/3",
            progress = if (isPremium) 0.5f else capsuleCount / 3f,
            color = Color(0xFF80DEEA)
        )

        Spacer(Modifier.height(20.dp))

        StatusItem(
            label = stringResource(R.string.home_recall_glimpses),
            value = "$remainingRecalls/$maxRecalls",
            progress = remainingRecalls.toFloat() / maxRecalls,
            color = purpleAI
        )

        Spacer(Modifier.height(32.dp))

        if (!isPremium) {
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purpleAI)
            ) {
                Text(stringResource(R.string.home_remove_limits), fontWeight = FontWeight.Bold, color = Color.Black)
            }
        } else {
            // Para quem é Premium, adicionamos um botão para gerenciar/ver a loja
            OutlinedButton(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, gold.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Diamond, contentDescription = null, tint = gold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_plan_details), color = gold, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: String, progress: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun HomeContent(
    padding: PaddingValues,
    neonGreen: Color,
    navController: NavController,
    notes: List<NoteEntity>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    userPhotoUrl: String?,
    moodPoints: List<Float>,
    onCalendarClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onStatusClick: () -> Unit,
    isPremium: Boolean,
    unreadNotifications: Int,
    onNoteClick: (NoteEntity) -> Unit
) {
    val dateFormatPattern = stringResource(R.string.home_date_format)
    val context = LocalContext.current
    val locale = remember(context) { context.resources.configuration.locales[0] }
    val formatter = remember(dateFormatPattern, locale) { DateTimeFormatter.ofPattern(dateFormatPattern, locale) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val formattedDate = remember(selectedDate, locale) {
        selectedDate.format(formatter).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(locale) else it.toString()
        }
    }

    val daysList = remember {
        val today = LocalDate.now()
        (0..30).map { today.minusDays(it.toLong()) }
    }

    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(stringResource(R.string.home_delete_memory_title), color = Color.White) },
            text = { Text(stringResource(R.string.home_delete_memory_desc), color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    noteToDelete?.let { onDeleteNote(it) }
                    noteToDelete = null
                }) {
                    Text(stringResource(R.string.home_delete), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(stringResource(R.string.home_cancel_action), color = Color.White)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = padding
    ) {
        item {
            HomeHeader(
                date = formattedDate,
                userPhotoUrl = userPhotoUrl,
                unreadNotifications = unreadNotifications,
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onCalendarClick = onCalendarClick,
                onNotificationsClick = onNotificationsClick,
                onStatusClick = onStatusClick,
                isPremium = isPremium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            CalendarRow(
                days = daysList,
                selectedDay = selectedDate,
                onDateSelected = onDateSelected
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (notes.isEmpty()) {
            item {
                EmptyStateLottie()
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            items(notes) { note ->
                val noteTime = Instant.ofEpochMilli(note.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                    .format(timeFormatter)

                val snippet = if (note.isTimeCapsule) stringResource(R.string.home_frozen_snippet) 
                              else if (note.isLocked) stringResource(R.string.home_protected_snippet) 
                              else {
                                note.contentHtml
                                    .replace(Regex("<[^>]*>"), "")
                                    .replace("&nbsp;", " ")
                                    .take(50)
                              }

                DiaryNoteCard(
                    emoji = note.emoji,
                    time = noteTime,
                    title = note.title,
                    content = snippet,
                    isLocked = note.isLocked,
                    isTimeCapsule = note.isTimeCapsule,
                    unlockDate = note.unlockDate,
                    onClick = { onNoteClick(note) },
                    onLongClick = { noteToDelete = note }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            MoodChartCard(
                points = moodPoints,
                onHeaderClick = { navController.navigate(Screen.Statistics.route) }
            )
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun NotificationBottomSheetContent(
    notifications: List<NotificationEntity>,
    onMarkAllAsRead: () -> Unit,
    onDeleteAll: () -> Unit,
    onDelete: (NotificationEntity) -> Unit,
    onNotificationClick: (NotificationEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.home_notifications),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onDeleteAll) {
                        Text(stringResource(R.string.home_clear_all), color = Color.Red.copy(alpha = 0.7f))
                    }
                }
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllAsRead) {
                        Text(stringResource(R.string.home_mark_all_read), color = Color(0xFF00FFC2))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.home_no_notifications), color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = { onNotificationClick(notification) },
                        onDelete = { onDelete(notification) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (notification.isRead) Color(0xFF1A1A1A) else Color(0xFF252525)
    val borderColor = if (notification.isRead) Color.Transparent else Color(0xFF00FFC2).copy(alpha = 0.3f)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        when (notification.type) {
                            "CAPSULE" -> Color(0xFF80DEEA).copy(alpha = 0.1f)
                            "DONATION" -> Color(0xFFBB86FC).copy(alpha = 0.1f)
                            else -> Color.White.copy(alpha = 0.1f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (notification.type) {
                        "CAPSULE" -> Icons.Default.AcUnit
                        "DONATION" -> Icons.Default.Favorite
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        "CAPSULE" -> Color(0xFF80DEEA)
                        "DONATION" -> Color(0xFFBB86FC)
                        else -> Color.White
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    notification.message,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun FabMenu(
    navController: NavController,
    isMenuExpanded: Boolean,
    rotation: Float,
    neonGreen: Color,
    onToggle: () -> Unit,
    canAddNote: Boolean,
    onLimitReached: () -> Unit,
    isToday: Boolean
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
    ) {
        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HubButton(Icons.Default.Map, stringResource(R.string.home_fab_traces), neonGreen) {
                    onToggle()
                    navController.navigate(Screen.PlacesMap.route)
                }

                HubButton(Icons.Default.BarChart, stringResource(R.string.home_fab_status), neonGreen) {
                    onToggle()
                    navController.navigate(Screen.Statistics.route)
                }

                HubButton(Icons.Default.AutoAwesome, stringResource(R.string.home_fab_gratitude), neonGreen) {
                    onToggle()
                    navController.navigate(Screen.Gratitude.route)
                }

                HubButton(Icons.Default.History, stringResource(R.string.home_fab_recall), neonGreen) {
                    onToggle()
                    navController.navigate(Screen.Recall.route)
                }

                HubButton(
                    icon = Icons.Default.Edit, 
                    label = stringResource(R.string.home_fab_note),
                    color = if (isToday) neonGreen else Color.Gray 
                ) {
                    onToggle()
                    if (isToday) {
                        if (canAddNote) {
                            navController.navigate(Screen.WriteNote.createRoute())
                        } else {
                            onLimitReached()
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = neonGreen,
            shape = CircleShape,
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer(rotationZ = rotation)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

