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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    val richTextState = viewModel.richTextState
    val uiState by viewModel.uiStateFlow.collectAsState()

    val neonGreen = Color(0xFF00FFC2)
    val surfaceDark = Color(0xFF1E1E1E)

    // Estados de interface (Menus e Diálogos)
    var showFormatMenu by remember { mutableStateOf(false) }
    var showEmojiMenu by remember { mutableStateOf(false) }
    var showMarkerMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showAppearanceMenu by remember { mutableStateOf(false) }
    
    // Estado da Fonte Selecionada para o menu (não altera o UI global)
    var selectedFontFamily by remember { mutableStateOf<FontFamily>(FontFamily.Default) }

    var selectedImageFullScreen by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.onVoiceClick(context.cacheDir)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        viewModel.onImageSelected(uri)
    }

    val canAddMoreImages = uiState.images.size < 3
    val onEmojiClick = { showEmojiMenu = !showEmojiMenu }

    // 1. Diálogo de Detalhes
    if (showDetailsDialog) {
        val noteText = richTextState.annotatedString.text
        NoteDetailsDialog(
            wordCount = if (noteText.isBlank()) 0 else noteText.trim().split("\\s+".toRegex()).size,
            charCount = noteText.length,
            hasAudio = state.audioPath != null,
            imageCount = uiState.images.size,
            date = "11 DE FEVEREIRO DE 2026",
            onDismiss = { showDetailsDialog = false },
            neonGreen = neonGreen
        )
    }

    // 2. Bottom Sheet de Estilo de Fonte (Aplica apenas ao texto novo)
    if (showAppearanceMenu) {
        AppearanceBottomSheet(
            selectedFontFamily = selectedFontFamily,
            onFontSelected = { 
                selectedFontFamily = it
                viewModel.updateFontFamily(it) // APLICA APENAS NA CAIXA DE TEXTO (DALI PRA FRENTE)
                showAppearanceMenu = false 
            },
            onDismiss = { showAppearanceMenu = false },
            neonGreen = neonGreen
        )
    }

    if (selectedImageFullScreen != null) {
        Dialog(onDismissRequest = { selectedImageFullScreen = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedImageFullScreen = null }) {
                AsyncImage(
                    model = selectedImageFullScreen,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Entrada", color = Color.White, fontSize = 18.sp) }, // Fonte padrão
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.White)
                        }
                        NoteOptionsOverflowMenu(
                            expanded = showOverflowMenu,
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
                            onLockClick = { /* PIN */ },
                            onTimeCapsuleClick = { /* Capsula */ },
                            neonGreen = neonGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().imePadding()) {
                AnimatedVisibility(visible = showFormatMenu) {
                    TextFormattingPanel(state = richTextState, isVisible = showFormatMenu)
                }
                AnimatedVisibility(visible = showMarkerMenu) {
                    FloatingColorMenu(
                        selectedTextColor = state.richTextState.currentSpanStyle.color,
                        selectedMarkerColor = state.richTextState.currentSpanStyle.background ?: Color.Transparent,
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
                    imageCount = state.images.size,
                    accentColor = neonGreen,
                    onFormatClick = {
                        showFormatMenu = !showFormatMenu
                        showEmojiMenu = false
                        showMarkerMenu = false
                    },
                    onEmojiClick = onEmojiClick,
                    onMarkerClick = {
                        showMarkerMenu = !showMarkerMenu
                        showFormatMenu = false
                        showEmojiMenu = false
                    },
                    onAddImageClick = { launcher.launch("image/*") },
                    onVoiceClick = {
                        handleVoiceClick(context, state.isRecording, state.audioPath, viewModel, permissionLauncher)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = state.title,
                                onValueChange = { viewModel.updateTitle(it) },
                                textStyle = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold),
                                cursorBrush = SolidColor(neonGreen)
                            )
                            Text("Humor: ${state.selectedHumor}", color = neonGreen, fontSize = 14.sp)
                        }
                        Text(state.selectedEmoji, fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // O EDITOR DE TEXTO RICO (Sem fontFamily global para permitir estilos individuais)
                    RichTextEditor(
                        state = richTextState,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
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

                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PhotoGrid(
                            images = uiState.images,
                            onRemove = { uri -> viewModel.removeImage(uri) },
                            onExpand = { uri -> selectedImageFullScreen = uri }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    VoiceNoteSection(
                        isRecording = state.isRecording,
                        recordingTime = state.recordingTime,
                        audioPath = state.audioPath,
                        viewModel = viewModel,
                        context = context,
                        permissionLauncher = permissionLauncher,
                        accentColor = neonGreen
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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun WriteNoteScreenPreview() {
    MaterialTheme {
        Surface(color = Color.Black) {
            WriteNoteScreen(onBack = { })
        }
    }
}
