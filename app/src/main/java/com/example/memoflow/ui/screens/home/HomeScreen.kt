package com.example.memoflow.ui.screens.home

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.memoflow.ui.components.home.*
import com.example.memoflow.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.memoflow.navigation.Screen
import com.example.memoflow.ui.theme.MemoFlowTheme
import java.time.Instant
import java.time.ZoneId
import com.example.memoflow.ui.screens.profile.ProfileViewModel
import com.example.memoflow.ui.screens.security.SecurityViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
    securityViewModel: SecurityViewModel = viewModel(factory = SecurityViewModel.Factory)
) {
    val context = LocalContext.current
    val neonGreen = Color(0xFF00FFC2)
    var isMenuExpanded by remember { mutableStateOf(false) }
    
    val notes by viewModel.notes.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val userSettings by profileViewModel.userSettings.collectAsState()
    val securitySettings by securityViewModel.userSettings.collectAsState()

    var noteToUnlock by remember { mutableStateOf<com.example.memoflow.data.local.entity.NoteEntity?>(null) }
    var pinInput by remember { mutableStateOf("") }

    val rotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 45f else 0f,
        label = "fab_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FabMenu(
                    navController = navController,
                    isMenuExpanded = isMenuExpanded,
                    rotation = rotation,
                    neonGreen = neonGreen,
                    onToggle = { isMenuExpanded = !isMenuExpanded },
                    canAddNote = notes.size < 3,
                    onLimitReached = {
                        Toast.makeText(context, "Limite de 3 notas por dia atingido!", Toast.LENGTH_SHORT).show()
                    }
                )
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
                userPhotoUrl = userSettings.profilePhotoUri,
                onNoteClick = { note ->
                    if (note.isLocked) {
                        noteToUnlock = note
                    } else {
                        navController.navigate(Screen.WriteNote.createRoute(note.id))
                    }
                }
            )
        }

        if (noteToUnlock != null) {
            AlertDialog(
                onDismissRequest = { noteToUnlock = null; pinInput = "" },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Memória Trancada", color = Color.White) },
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
                            val id = noteToUnlock?.id
                            noteToUnlock = null
                            pinInput = ""
                            navController.navigate(Screen.WriteNote.createRoute(id))
                        } else {
                            Toast.makeText(context, "PIN Incorreto!", Toast.LENGTH_SHORT).show()
                            pinInput = ""
                        }
                    }) {
                        Text("DESBLOQUEAR", color = neonGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noteToUnlock = null; pinInput = "" }) {
                        Text("CANCELAR", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun HomeContent(
    padding: PaddingValues,
    neonGreen: Color,
    navController: NavController,
    notes: List<com.example.memoflow.data.local.entity.NoteEntity>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDeleteNote: (com.example.memoflow.data.local.entity.NoteEntity) -> Unit,
    userPhotoUrl: String?,
    onNoteClick: (com.example.memoflow.data.local.entity.NoteEntity) -> Unit
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

    var noteToDelete by remember { mutableStateOf<com.example.memoflow.data.local.entity.NoteEntity?>(null) }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Apagar memória?") },
            text = { Text("Esta ação não pode ser desfeita. Deseja apagar esta nota para sempre?") },
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
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.Gray
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
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            CalendarRow(
                days = daysList,
                selectedDay = selectedDate,
                neonGreen = neonGreen,
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

                val snippet = if (note.isLocked) "Memória protegida por PIN" else {
                    note.contentHtml
                        .replace(Regex("<[^>]*>"), "")
                        .replace("&nbsp;", " ")
                        .take(50)
                }

                DiaryNoteCard(
                    emoji = if (note.isLocked) "🔒" else note.emoji,
                    time = noteTime,
                    title = if (note.isLocked) "Trancada" else note.title,
                    content = if (!note.isLocked && snippet.length >= 50) "$snippet..." else snippet,
                    neonGreen = neonGreen,
                    onClick = { onNoteClick(note) },
                    onLongClick = { noteToDelete = note }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            MoodChartCard(neonGreen = neonGreen)
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
    onLimitReached: () -> Unit
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
                HubButton(Icons.Default.BarChart, "Status", neonGreen) {
                    onToggle()
                    navController.navigate("construction")
                }

                HubButton(Icons.Default.Flag, "Metas", neonGreen) {
                    onToggle()
                    navController.navigate("construction")
                }

                HubButton(Icons.Default.Edit, "Nota", neonGreen) {
                    onToggle()
                    if (canAddNote) {
                        navController.navigate(Screen.WriteNote.createRoute())
                    } else {
                        onLimitReached()
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

@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun HomeScreenPreview() {
    MemoFlowTheme(darkTheme = true) {
        val previewNavController = rememberNavController()
        Scaffold(
            containerColor = Color.Black
        ) { padding ->
            HomeContent(
                padding = padding,
                neonGreen = Color(0xFF00FFC2),
                navController = previewNavController,
                notes = emptyList(),
                selectedDate = LocalDate.now(),
                onDateSelected = {},
                onDeleteNote = {},
                userPhotoUrl = null,
                onNoteClick = {}
            )
        }
    }
}
