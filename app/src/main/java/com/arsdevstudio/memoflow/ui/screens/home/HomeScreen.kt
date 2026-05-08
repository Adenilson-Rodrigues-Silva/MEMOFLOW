package com.arsdevstudio.memoflow.ui.screens.home

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.data.local.entity.UserEntity
import com.arsdevstudio.memoflow.ui.components.home.*
import com.arsdevstudio.memoflow.ui.viewmodel.HomeViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
    securityViewModel: SecurityViewModel = viewModel(factory = SecurityViewModel.Factory),
    statsViewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory),
    writeNoteViewModel: WriteNoteViewModel = viewModel(factory = WriteNoteViewModel.Factory),
    recallViewModel: RecallViewModel = viewModel(factory = RecallViewModel.Factory),
    gratitudeViewModel: GratitudeViewModel = viewModel(factory = GratitudeViewModel.Factory)
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
                        canAddNote = viewModel.canAddNote(),
                        onLimitReached = {
                            Toast.makeText(context, "Limite de 3 notas diárias atingido!", Toast.LENGTH_SHORT).show()
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
                onStoreClick = { navController.navigate(Screen.Store.route) },
                onStatusClick = { showStatusSheet = true },
                isPremium = isPremium,
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
                title = { Text(if (isCapsule) "Acessar Cápsula" else "Memória Trancada", color = Color.White) },
                text = {
                    Column {
                        Text("Digite seu PIN de 4 dígitos para acessar.", color = Color.Gray, fontSize = 14.sp)
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
                            Toast.makeText(context, "PIN Incorreto!", Toast.LENGTH_SHORT).show()
                            pinInput = ""
                        }
                    }) {
                        Text("VERIFICAR", color = neonGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noteToUnlock = null; pinInput = "" }) {
                        Text("CANCELAR", color = Color.White)
                    }
                }
            )
        }

        if (showMeltOptions) {
            val isReady = noteToUnlock?.unlockDate?.let { it <= System.currentTimeMillis() } ?: false
            AlertDialog(
                onDismissRequest = { showMeltOptions = false; noteToUnlock = null; pinInput = "" },
                containerColor = Color(0xFF1A1A1A),
                title = { Text(if(isReady) "O gelo derreteu!" else "Ainda está congelada", color = Color.White) },
                text = { Text(if(isReady) "Esta memória está pronta para ser revelada. Deseja descongelar para sempre?" else "O tempo de espera ainda não acabou, mas você pode dar uma espiadinha silenciosa.", color = Color.Gray) },
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
                                Text("DERRETER PARA SEMPRE", color = Color.Black, fontWeight = FontWeight.Bold)
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
                                Text("SÓ ESPIAR (CONGELADA)", color = iceBlue)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = { showMeltOptions = false; noteToUnlock = null; pinInput = "" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("CANCELAR", color = Color.White)
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
            if (isPremium) "Status Premium Ativo" else "Status do seu Flow",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            if (isPremium) "Você tem acesso ilimitado a quase tudo!" else "Gerencie seu uso diário e limites.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(32.dp))

        StatusItem(
            label = "Notas de hoje",
            value = "$noteCountToday/3",
            progress = noteCountToday / 3f,
            color = neonGreen
        )

        Spacer(Modifier.height(20.dp))

        StatusItem(
            label = "Cápsulas do Tempo",
            value = if (isPremium) "$capsuleCount/∞" else "$capsuleCount/3",
            progress = if (isPremium) 0.5f else capsuleCount / 3f,
            color = Color(0xFF80DEEA)
        )

        Spacer(Modifier.height(20.dp))

        StatusItem(
            label = "Vislumbres (Recall)",
            value = "$remainingRecalls/$maxRecalls restantes",
            progress = remainingRecalls.toFloat() / maxRecalls,
            color = purpleAI
        )

        if (!isPremium) {
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purpleAI)
            ) {
                Text("REMOVER LIMITES AGORA", fontWeight = FontWeight.Bold, color = Color.Black)
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
    onStoreClick: () -> Unit,
    onStatusClick: () -> Unit,
    isPremium: Boolean,
    onNoteClick: (NoteEntity) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("pt", "BR")) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val formattedDate = remember(selectedDate) {
        selectedDate.format(formatter).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString()
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
            title = { Text("Apagar memória?", color = Color.White) },
            text = { Text("Esta ação não pode ser desfeita. Deseja apagar esta nota para sempre?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    noteToDelete?.let { onDeleteNote(it) }
                    noteToDelete = null
                }) {
                    Text("Apagar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancelar", color = Color.White)
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
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onCalendarClick = onCalendarClick,
                onStoreClick = onStoreClick,
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

                val snippet = if (note.isTimeCapsule) "Memória congelada no tempo" 
                              else if (note.isLocked) "Memória protegida por PIN" 
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
                    neonGreen = neonGreen,
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
                HubButton(Icons.Default.Map, "Rastros", neonGreen) {
                    onToggle()
                    navController.navigate(Screen.PlacesMap.route)
                }

                HubButton(Icons.Default.BarChart, "Status", neonGreen) {
                    onToggle()
                    navController.navigate(Screen.Statistics.route)
                }

                HubButton(Icons.Default.AutoAwesome, "Gratidão", neonGreen) {
                    onToggle()
                    navController.navigate(Screen.Gratitude.route)
                }

                HubButton(Icons.Default.History, "Relembrar", neonGreen) {
                    onToggle()
                    navController.navigate(Screen.Recall.route)
                }

                HubButton(
                    icon = Icons.Default.Edit, 
                    label = "Nota", 
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

