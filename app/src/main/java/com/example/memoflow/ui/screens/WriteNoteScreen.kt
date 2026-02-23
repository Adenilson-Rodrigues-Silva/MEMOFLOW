package com.example.memoflow.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.memoflow.ui.components.PhotoGrid
import com.example.memoflow.ui.components.TextFormattingPanel
import com.example.memoflow.viewmodel.WriteNoteViewModel
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WriteNoteScreen(
    onBack: () -> Unit,
    viewModel: WriteNoteViewModel = viewModel()
) {






    val context = androidx.compose.ui.platform.LocalContext.current
    val state = viewModel.uiState
    val richTextState = viewModel.richTextState // estado rich text
    val uiState by viewModel.uiStateFlow.collectAsState()

    val neonGreen = Color(0xFF00FFC2)
    val surfaceDark = Color(0xFF1E1E1E)

    // Estados de interface (Menus e Diálogos)
    var showFormatMenu by remember { mutableStateOf(false) }
    var showEmojiMenu by remember { mutableStateOf(false) }
    var showMarkerMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAudioDeleteDialog by remember { mutableStateOf(false) } // NOVO: Estado para confirmação do áudio
    var indexToDelete by remember { mutableStateOf(-1) }
    var isFormattingVisible by remember { mutableStateOf(false) }


    var selectedImageFullScreen by remember { mutableStateOf<Uri?>(null) }



    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Aqui está o segredo: avisar a ViewModel que uma imagem foi escolhida
        uri?.let { viewModel.onImageSelected(it) }
    }


    // Launcher para permissão de áudio
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.onVoiceClick(context.cacheDir)
    }

    // Launcher para Galeria de Imagens
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        viewModel.onImageSelected(uri)
    }

    // Define se o botão está habilitado (máximo 3 imagens) [cite: 2026-02-08]
    val canAddMoreImages = uiState.images.size < 3

// Define o que acontece ao clicar
    val onAddImageClick = { galleryLauncher.launch("image/*") }
    val onEmojiClick = {
        // Inverte o estado: se está aberto, fecha; se está fechado, abre [cite: 2026-02-08]
        showEmojiMenu = !showEmojiMenu
    }



    if (selectedImageFullScreen != null) {
        Dialog(onDismissRequest = { selectedImageFullScreen = null }) {
            Box(modifier = Modifier.fillMaxSize().clickable { selectedImageFullScreen = null }) {
                AsyncImage(
                    model = selectedImageFullScreen,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth()
                )
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Entrada", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Menu de Formatação (B)
                AnimatedVisibility(visible = showFormatMenu) {
                    TextFormattingPanel(
                        state = richTextState,
                        isVisible = showFormatMenu
                    )
                }

                // 2. Menu de Cores/Marcador
                AnimatedVisibility(visible = showMarkerMenu) {
                    FloatingColorMenu(
                        onTextColorSelected = { color -> viewModel.updateTextColor(color) },
                        onMarkerColorSelected = { color -> viewModel.applyMarker(color) },
                        neonGreen = neonGreen
                    )
                }

                // 3. Menu de Emojis (Corrigido para evitar o erro de Alignment)
                AnimatedVisibility(
                    visible = showEmojiMenu,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally) // CORREÇÃO: Usando CenterHorizontally para Column [cite: 2026-02-08]
                        .padding(bottom = 15.dp)
                ) {
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .padding(16.dp)
                            .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    ) {
                        EmojiSelector(
                            emojis = uiState.diaryEmojis,
                            selectedEmoji = uiState.selectedEmoji,
                            onEmojiSelected = { emoji, humor ->
                                viewModel.updateEmoji(emoji, humor)
                                showEmojiMenu = false // Fecha após escolher [cite: 2026-02-08]
                            }
                        )
                    }
                }

                // 4. A Barra de Ferramentas Principal
                NoteBottomToolbar(
                    imageCount = state.images.size,
                    accentColor = neonGreen,
                    onFormatClick = {
                        showFormatMenu = !showFormatMenu
                        showEmojiMenu = false
                        showMarkerMenu = false
                    },
                    onEmojiClick = onEmojiClick, // Usando sua variável que inverte o estado [cite: 2026-02-08]
                    onMarkerClick = {
                        showMarkerMenu = !showMarkerMenu
                        showFormatMenu = false
                        showEmojiMenu = false
                    },
                    onAddImageClick = {
                        launcher.launch("image/*")
                    },
                    onVoiceClick = {
                        if (state.isRecording) {
                            viewModel.onVoiceClick(context.cacheDir)
                        } else if (state.audioPath != null) {
                            android.widget.Toast.makeText(context, "Já existe um áudio. Apague para gravar outro.", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val permission = android.Manifest.permission.RECORD_AUDIO
                            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                context, permission
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (isGranted) {
                                viewModel.onVoiceClick(context.cacheDir)
                            } else {
                                permissionLauncher.launch(permission)
                            }
                        }
                    },
                    onSaveClick = {
                        if (richTextState.annotatedString.text.isNotBlank()) {
                            viewModel.saveNote()
                            onBack()
                        } else {
                            onBack()
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "11 DE FEVEREIRO DE 2026",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceDark.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header: Título e Humor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = state.title,
                                onValueChange = { viewModel.updateTitle(it) },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(neonGreen)
                            )
                            Text(
                                "Humor: ${state.selectedHumor}",
                                color = neonGreen,
                                fontSize = 14.sp
                            )
                        }
                        Text(state.selectedEmoji, fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // EDITOR DE TEXTO RICO
                    RichTextEditor(
                        state = richTextState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp),
                        colors = RichTextEditorDefaults.richTextEditorColors(
                            containerColor = Color.Transparent,
                            cursorColor = neonGreen,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            textColor = Color.White.copy(alpha = 0.8f)
                        ),
                        textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SLOT PARA AS 3 IMAGENS
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Chame os 3 quadrados de uma vez só!
                        PhotoGrid(
                            images = uiState.images,
                            onRemove = { uri -> viewModel.removeImage(uri) },
                            onExpand = { uri -> selectedImageFullScreen = uri }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // PLAYER DE ÁUDIO CORRIGIDO
                    AudioPlayerComponent(
                        accentColor = neonGreen,
                        isRecording = state.isRecording,
                        isPlaying = viewModel.isPlaying,
                        currentTime = state.recordingTime,
                        audioPath = state.audioPath,
                        onPlayClick = {
                            // CORREÇÃO: Chama o play da ViewModel
                            viewModel.playAudio()
                        },
                        onDeleteClick = {
                            // CORREÇÃO: Agora abre o diálogo em vez de apagar direto
                            showAudioDeleteDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Dono do Chronos",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // --- DIÁLOGOS DE CONFIRMAÇÃO ---

    // 1. Diálogo para Imagens
    if (showAudioDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showAudioDeleteDialog = false },
            title = { Text("Apagar Gravação?") },
            text = { Text("Deseja remover o áudio desta nota para gravar um novo?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAudio()
                    showAudioDeleteDialog = false
                }) { Text("Apagar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showAudioDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // 2. Diálogo para Áudio (NOVO E CORRIGIDO)
    if (showAudioDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showAudioDeleteDialog = false },
            title = { Text("Apagar Gravação?") },
            text = { Text("Deseja remover o áudio desta nota para gravar um novo?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAudio()
                    showAudioDeleteDialog = false
                }) {
                    Text("Apagar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAudioDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- COMPONENTES AUXILIARES ---

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
                ToolbarButton(Icons.Default.Brush, "Marcador", onClick = onMarkerClick)
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

@Composable
fun FloatingFormatMenu(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onBulletClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onSizeClick: (androidx.compose.ui.unit.TextUnit) -> Unit,
    isBoldActive: Boolean,
    neonGreen: Color,
    isPlaying: Boolean
) {
    Surface(
        color = Color(0xFF2A2A2A),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBoldClick) {
                    Icon(Icons.Default.FormatBold, null, tint = if (isBoldActive) neonGreen else Color.White)
                }
                IconButton(onClick = onItalicClick) { Icon(Icons.Default.FormatItalic, null, tint = Color.White) }
                IconButton(onClick = onUnderlineClick) { Icon(Icons.Default.FormatUnderlined, null, tint = Color.White) }
                IconButton(onClick = onBulletClick) { Icon(Icons.Default.FormatListBulleted, null, tint = neonGreen) }
                IconButton(onClick = onQuoteClick) { Icon(Icons.Default.FormatQuote, null, tint = neonGreen) }
            }
            Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Text("Tam:", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))
                TextButton(onClick = { onSizeClick(14.sp) }) { Text("P", color = Color.White) }
                TextButton(onClick = { onSizeClick(18.sp) }) { Text("M", color = Color.White) }
                TextButton(onClick = { onSizeClick(24.sp) }) { Text("G", color = Color.White) }
            }
        }
    }
}

@Composable
fun FloatingColorMenu(
    onTextColorSelected: (Color) -> Unit,
    onMarkerColorSelected: (Color) -> Unit,
    neonGreen: Color
) {
    val colors = listOf(neonGreen, Color(0xFFFF00E5), Color(0xFF00B2FF), Color(0xFFE6FB04), Color.White)
    Surface(color = Color(0xFF2A2A2A), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Cor do Texto", color = Color.Gray, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colors.forEach { color ->
                    Box(modifier = Modifier.size(25.dp).background(color, CircleShape).clickable { onTextColorSelected(color) })
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Marcador (Fundo)", color = Color.Gray, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colors.forEach { color ->
                    Box(modifier = Modifier.size(25.dp).border(1.dp, color, CircleShape).background(color.copy(alpha = 0.3f), CircleShape).clickable { onMarkerColorSelected(color) })
                }
            }
        }
    }
}


@Composable
fun AudioPlayerComponent(
    accentColor: Color,
    isRecording: Boolean,
    isPlaying: Boolean, // A "chave" que adicionamos
    currentTime: Int,
    audioPath: String?,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BOTÃO DE PLAY/PAUSE/MIC
            IconButton(
                onClick = { onPlayClick() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when {
                        isRecording -> Icons.Default.Mic
                        isPlaying -> Icons.Default.Pause
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = "Play/Pause",
                    tint = when {
                        isRecording -> Color.Red
                        isPlaying -> accentColor // Fica Neon Green (0xFF00FFC2)
                        else -> Color.White
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            val timeText = if (currentTime < 10) "0:0$currentTime" else "0:$currentTime"
            Text(
                text = "$timeText / 0:30",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // BARRA DE PROGRESSO
            LinearProgressIndicator(
                progress = currentTime / 30f,
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = when {
                    isRecording -> Color.Red
                    isPlaying -> accentColor
                    else -> accentColor.copy(alpha = 0.5f)
                },
                trackColor = Color.DarkGray
            )

            // LIXEIRA (Só aparece se tiver áudio e não estiver gravando)
            if (audioPath != null && !isRecording) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}