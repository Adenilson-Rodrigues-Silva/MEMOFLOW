package com.example.memoflow.ui.screens.note

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.memoflow.ui.components.EmojiSelector
import com.example.memoflow.ui.components.FloatingColorMenu
import com.example.memoflow.ui.components.PhotoGrid
import com.example.memoflow.ui.components.TextFormattingPanel
import com.example.memoflow.ui.components.VoiceNoteSection
import com.example.memoflow.ui.components.handleVoiceClick
import com.example.memoflow.ui.components.writenote.AppearanceBottomSheet
import com.example.memoflow.ui.components.writenote.NoteDetailsDialog
import com.example.memoflow.ui.components.writenote.NoteOptionsOverflowMenu
import com.example.memoflow.ui.screens.security.SecurityViewModel
import com.example.memoflow.viewmodel.WriteNoteViewModel
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WriteNoteScreen(
    onBack: () -> Unit,
    noteId: Long? = null,
    readOnly: Boolean = false,
    viewModel: WriteNoteViewModel = viewModel(factory = WriteNoteViewModel.Factory),
    securityViewModel: SecurityViewModel = viewModel(factory = SecurityViewModel.Factory)
) {
    val context = LocalContext.current
    val richTextState = viewModel.richTextState
    val uiState by viewModel.uiStateFlow.collectAsState()
    val securitySettings by securityViewModel.userSettings.collectAsState()

    val neonGreen = Color(0xFF00FFC2)
    val iceBlue = Color(0xFF80DEEA)
    val surfaceDark = Color(0xFF1E1E1E)

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Monitora a mudança de posição do cursor para rolar automaticamente
    LaunchedEffect(richTextState.selection) {
        // Pequeno delay para garantir que o layout atualizou
        coroutineScope.launch {
            // Rola para o final se estiver editando (comportamento simples mas eficaz)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snow_effects")
    val snowMove by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart),
        label = "snow"
    )

    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            viewModel.loadNote(noteId)
        }
    }

    var showFormatMenu by remember { mutableStateOf(false) }
    var showEmojiMenu by remember { mutableStateOf(false) }
    var showMarkerMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showAppearanceMenu by remember { mutableStateOf(false) }
    var showLockConfirmDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var selectedFontFamily by remember { mutableStateOf<FontFamily>(FontFamily.Default) }
    var selectedImageFullScreen by remember { mutableStateOf<Uri?>(null) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.onVoiceClick(context.cacheDir)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onImageSelected(context, uri)
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis
                    if (selectedDate != null && selectedDate > System.currentTimeMillis()) {
                        viewModel.setTimeCapsule(selectedDate)
                        viewModel.saveNote()
                        Toast.makeText(context, "Nota enviada para o futuro! ❄️", Toast.LENGTH_LONG).show()
                        onBack()
                    }
                    showDatePicker = false
                }) { Text("CONGELAR", color = iceBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("CANCELAR", color = Color.White) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showLockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLockConfirmDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text(if (uiState.isLocked) "Destrancar Memória?" else "Trancar Memória?", color = Color.White) },
            text = { 
                Text(
                    if (uiState.isLocked) "Deseja remover o bloqueio desta nota?" else "Tem certeza que deseja trancar esta memória? Ela só poderá ser aberta com o seu PIN.",
                    color = Color.Gray
                ) 
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleLock()
                    viewModel.saveNote()
                    showLockConfirmDialog = false
                    Toast.makeText(context, if (!uiState.isLocked) "Memória Trancada!" else "Memória Destrancada!", Toast.LENGTH_SHORT).show()
                    onBack()
                }) { Text("CONFIRMAR", color = neonGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showLockConfirmDialog = false }) { Text("CANCELAR", color = Color.White) }
            }
        )
    }

    if (showDetailsDialog) {
        NoteDetailsDialog(
            wordCount = richTextState.annotatedString.text.trim().split("\\s+".toRegex()).size,
            charCount = richTextState.annotatedString.text.length,
            hasAudio = uiState.audioPath != null,
            imageCount = uiState.images.size,
            date = "HOJE",
            onDismiss = { showDetailsDialog = false },
            neonGreen = neonGreen
        )
    }

    if (selectedImageFullScreen != null) {
        Dialog(onDismissRequest = { selectedImageFullScreen = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedImageFullScreen = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedImageFullScreen,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
                IconButton(
                    onClick = { selectedImageFullScreen = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(if (readOnly) "Modo Espiadinha ❄️" else "Entrada", color = Color.White, fontSize = 18.sp)
                }, 
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                },
                actions = {
                    if (readOnly) {
                        TextButton(onClick = onBack) {
                            Text("SAIR", color = iceBlue, fontWeight = FontWeight.ExtraBold)
                        }
                    } else {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.White)
                        }
                        NoteOptionsOverflowMenu(
                            expanded = showOverflowMenu,
                            isLocked = uiState.isLocked,
                            onDismissRequest = { showOverflowMenu = false },
                            onShareClick = { /* PDF */ },
                            onFontStyleClick = { 
                                showAppearanceMenu = true
                                showOverflowMenu = false
                            },
                            onDetailsClick = { 
                                showDetailsDialog = true
                                showOverflowMenu = false
                            },
                            onDeleteClick = { /* Deletar */ },
                            onLockClick = { 
                                if (securitySettings.pin.isNullOrEmpty()) {
                                    Toast.makeText(context, "Defina um PIN nas configurações primeiro!", Toast.LENGTH_LONG).show()
                                } else {
                                    showLockConfirmDialog = true
                                }
                                showOverflowMenu = false
                            },
                            onTimeCapsuleClick = { 
                                showDatePicker = true
                                showOverflowMenu = false
                            },
                            neonGreen = neonGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            if (!readOnly) {
                Column(modifier = Modifier.fillMaxWidth().imePadding()) {
                    AnimatedVisibility(visible = showFormatMenu) {
                        TextFormattingPanel(state = richTextState, isVisible = showFormatMenu)
                    }
                    AnimatedVisibility(visible = showMarkerMenu) {
                        FloatingColorMenu(
                            selectedTextColor = richTextState.currentSpanStyle.color,
                            selectedMarkerColor = (richTextState.currentSpanStyle.background as? Color) ?: Color.Transparent,
                            onTextColorSelected = { color -> viewModel.updateTextColor(color) },
                            onMarkerColorSelected = { color -> viewModel.applyMarker(color) },
                            onDismiss = { showMarkerMenu = false },
                            neonGreen = neonGreen
                        )
                    }
                    AnimatedVisibility(
                        visible = showEmojiMenu,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 15.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E1E1E),
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 8.dp,
                            modifier = Modifier.padding(16.dp).border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        ) {
                            EmojiSelector(
                                emojis = uiState.diaryEmojis,
                                selectedEmoji = uiState.selectedEmoji,
                                onEmojiSelected = { emoji, humor ->
                                    viewModel.updateEmoji(emoji, humor)
                                    showEmojiMenu = false
                                }
                            )
                        }
                    }
                    NoteBottomToolbar(
                        imageCount = uiState.images.size,
                        accentColor = if (uiState.isTimeCapsule) iceBlue else neonGreen,
                        onFormatClick = { 
                            showFormatMenu = !showFormatMenu
                            showEmojiMenu = false
                            showMarkerMenu = false
                        },
                        onEmojiClick = { 
                            showEmojiMenu = !showEmojiMenu
                            showFormatMenu = false
                            showMarkerMenu = false
                        },
                        onMarkerClick = { 
                            showMarkerMenu = !showMarkerMenu
                            showFormatMenu = false
                            showEmojiMenu = false
                        },
                        onAddImageClick = { launcher.launch("image/*") },
                        onVoiceClick = { handleVoiceClick(context, uiState.isRecording, uiState.audioPath, viewModel, permissionLauncher) },
                        onSaveClick = {
                            viewModel.saveNote()
                            onBack()
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                if (uiState.isTimeCapsule) {
                    Text(
                        text = "CONGELADA ATÉ ${uiState.unlockDate?.let { 
                            java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it)) 
                        }}",
                        color = iceBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = surfaceDark.copy(alpha = if (readOnly) 0.4f else 0.6f)),
                    shape = RoundedCornerShape(24.dp),
                    border = if (readOnly) BorderStroke(1.dp, iceBlue.copy(alpha = 0.3f)) else null
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                androidx.compose.foundation.text.BasicTextField(
                                    value = uiState.title,
                                    onValueChange = { if(!readOnly) viewModel.updateTitle(it) },
                                    readOnly = readOnly,
                                    enabled = !readOnly,
                                    textStyle = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold),
                                    cursorBrush = SolidColor(if (uiState.isTimeCapsule) iceBlue else neonGreen)
                                )
                                Text("Humor: ${uiState.selectedHumor}", color = if (uiState.isTimeCapsule) iceBlue else neonGreen, fontSize = 14.sp)
                            }
                            Text(uiState.selectedEmoji, fontSize = 40.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        RichTextEditor(
                            state = richTextState,
                            readOnly = readOnly,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                            colors = RichTextEditorDefaults.richTextEditorColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                textColor = Color.White.copy(alpha = 0.8f)
                            ),
                            textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PhotoGrid(
                                images = uiState.images,
                                onRemove = { if(!readOnly) viewModel.removeImage(it) },
                                onExpand = { selectedImageFullScreen = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        VoiceNoteSection(
                            isRecording = uiState.isRecording,
                            recordingTime = uiState.recordingTime,
                            audioPath = uiState.audioPath,
                            viewModel = viewModel,
                            context = context,
                            permissionLauncher = permissionLauncher,
                            accentColor = if (uiState.isTimeCapsule) iceBlue else neonGreen
                        )
                    }
                }
                
                // Espaço extra generoso para o teclado não cobrir nada
                Spacer(modifier = Modifier.height(300.dp))
            }

            if (readOnly) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val snowflakeCount = 45
                    for (i in 0 until snowflakeCount) {
                        val x = ( (i * 123f + (snowMove * 0.5f)) % size.width )
                        val y = ( (i * 87f + snowMove) % size.height )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteBottomToolbar(
    imageCount: Int,
    accentColor: Color,
    onFormatClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onMarkerClick: () -> Unit,
    onAddImageClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val canAddMoreImages = imageCount < 3
    Surface(
        color = Color(0xFF121212),
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                ToolbarButton(Icons.Default.Mic, "Voz", onClick = onVoiceClick)
                ToolbarButton(Icons.Default.FormatBold, "Formatar", onClick = onFormatClick)
                ToolbarButton(Icons.Default.Face, "Emoji", onClick = onEmojiClick)
                IconButton(onClick = onAddImageClick, enabled = canAddMoreImages) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Adicionar Imagem",
                        tint = if (canAddMoreImages) Color.Gray else Color.DarkGray.copy(alpha = 0.5f)
                    )
                }
                ToolbarButton(icon = Icons.Default.Brush, desc = "Marcador", onClick = onMarkerClick)
            }
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = accentColor,
                shape = CircleShape,
                modifier = Modifier.size(56.dp).offset(y = (-10).dp)
            ) {
                Icon(Icons.Default.Check, null, tint = Color.Black)
            }
        }
    }
}

@Composable
fun ToolbarButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, desc, tint = Color.Gray)
    }
}
