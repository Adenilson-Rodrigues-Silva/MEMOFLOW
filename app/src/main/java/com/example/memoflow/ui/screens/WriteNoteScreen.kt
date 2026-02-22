package com.example.memoflow.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.memoflow.viewmodel.WriteNoteViewModel
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WriteNoteScreen(

    onBack: () -> Unit,
    viewModel: WriteNoteViewModel = viewModel()
) {



    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {uri: android.net.Uri? ->
        viewModel.onImageSelected(uri)
    }

    val state = viewModel.uiState
    val richTextState = viewModel.richTextState

    val neonGreen = Color(0xFF00FFC2)
    val surfaceDark = Color(0xFF1E1E1E)

    // Estados de interface (Menus)
    var showFormatMenu by remember { mutableStateOf(false) }
    var showEmojiMenu by remember { mutableStateOf(false) }
    var showMarkerMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var imageToDelete by remember { mutableStateOf<android.net.Uri?>(null) }

    var indexToDelete by remember { mutableStateOf(-1) } // -1 significa nenhum selecionado

    Scaffold(

        containerColor = Color.Black,
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
            Column {
                // Menu de Formatação (Negrito, Itálico, Sublinhado)
                // Menu de Formatação Atualizado
                AnimatedVisibility(visible = showFormatMenu) {
                    FloatingFormatMenu(
                        onBoldClick = { viewModel.toggleBold() },
                        onItalicClick = { viewModel.toggleItalic() },
                        onUnderlineClick = { viewModel.toggleUnderline() },
                        onBulletClick = { viewModel.toggleBulletList() }, // Nova função
                        onQuoteClick = { viewModel.toggleQuote() },       // Nova função
                        onSizeClick = { size -> viewModel.updateFontSize(size) }, // Nova função
                        isBoldActive = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                        neonGreen = neonGreen
                    )
                }



                // Menu de Cores Atualizado (Substituindo o antigo FloatingMarkerMenu)
                AnimatedVisibility(visible = showMarkerMenu) {
                    FloatingColorMenu(
                        onTextColorSelected = { color -> viewModel.updateTextColor(color) },
                        onMarkerColorSelected = { color -> viewModel.applyMarker(color) },
                        neonGreen = neonGreen
                    )
                }

                // Menu de Emojis
                AnimatedVisibility(visible = showEmojiMenu) {
                    FloatingEmojiMenu(
                        onEmojiSelected = { emoji, humor ->
                            viewModel.updateEmoji(emoji, humor)
                            showEmojiMenu = false
                        },
                        neonGreen = neonGreen
                    )
                }

                NoteBottomToolbar(
                    imageCount = state.images.size, // Resolve o erro de 'imageCount' [cite: 2026-02-08]
                    accentColor = neonGreen,
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
                    onAddImageClick = {
                        launcher.launch("image/*")  // [cite: 2026-02-08]
                        // Lógica para abrir galeria (Máximo 3 fotos) [cite: 2026-02-08]
                        // viewModel.pickImage()
                    },
                    onVoiceClick = {
                        // Lógica do microfone (Projeto de Amanhã!) [cite: 2026-02-08]
                    },
                    onSaveClick = {

                        if (state.richTextState.annotatedString.text.isNotBlank()) {
                            viewModel.saveNote()
                            onBack()
                        }


                        // Lógica para salvar a nota no banco de dados
                        onBack() // Por enquanto, apenas volta para a tela anterior
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
                text = "11 DE FEVEREIRO DE 2026", // Data dinâmica para o Chronos Diary
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
                            // Título ainda usa BasicTextField por ser simples (sem Rich Text)
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

                    // O EDITOR DE TEXTO RICO (Onde a mágica acontece)
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

                    // --- SLOT PARA AS 3 IMAGENS (Pilar do Projeto) ---
                    // --- SLOT PARA AS 3 IMAGENS (Corrigido sem Skydoves) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0 until 3) {
                            val imageUri = state.images.getOrNull(i)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray.copy(alpha = 0.3f))
                                    // DETECÇÃO DE TOQUE LONGO PARA APAGAR [cite: 2026-02-08]
                                    .combinedClickable(
                                        onClick = { /* Opcional: abrir imagem cheia */ },
                                        onLongClick = {
                                            if (imageUri != null) {
                                                indexToDelete =
                                                    i // Agora salvamos a POSIÇÃO [cite: 2026-02-08]
                                                showDeleteDialog = true
                                            }
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUri != null) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "Imagem da nota",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Add, null, tint = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    AudioPlayerComponent(neonGreen)
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Apagar Imagem?") },
            confirmButton = {
                TextButton(onClick = {
                    if (indexToDelete != -1) {
                        viewModel.removeImageAtIndex(indexToDelete)
                    }
                    showDeleteDialog = false
                    indexToDelete = -1
                }) {
                    Text("Apagar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun NoteBottomToolbar(
    imageCount: Int,               // Conta quantas fotos já foram adicionadas (Máximo 3) [cite: 2026-02-08]
    accentColor: Color,            // A cor Neon Green que define o estilo do app
    onFormatClick: () -> Unit,     // Abre o menu de Negrito, Listas e Tamanhos
    onEmojiClick: () -> Unit,      // Abre o menu dos 7 sentimentos
    onMarkerClick: () -> Unit,     // Abre o menu de cores e marcador
    onAddImageClick: () -> Unit,   // Função para abrir a galeria
    onVoiceClick: () -> Unit,      // Aciona o sensor de áudio de 30s [cite: 2026-02-08]
    onSaveClick: () -> Unit        // Salva a nota no banco de dados e sai
) {
    // REGRA DE NEGÓCIO: Só permite adicionar imagem se tiver menos de 3 [cite: 2026-02-08]
    val canAddMoreImages = imageCount < 3

    Surface(
        color = Color(0xFF121212), // Fundo escuro profundo para contraste Cyberpunk
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) // Cantos arredondados no topo
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // GRUPO DE FERRAMENTAS (Esquerda e Centro)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 1. MICROFONE: Para transcrição de voz (Projeto de amanhã) [cite: 2026-02-08]
                ToolbarButton(Icons.Default.Mic, "Voz", onClick = onVoiceClick)

                // 2. FORMATAR: Negrito, Itálico, Listas e Fontes
                ToolbarButton(Icons.Default.FormatBold, "Formatar", onClick = onFormatClick)

                // 3. EMOJI: Seleção de humor (Os 7 sentimentos) [cite: 2026-02-08]
                ToolbarButton(Icons.Default.Face, "Emoji", onClick = onEmojiClick)

                // 4. IMAGEM: Com trava de segurança para limite de 3 [cite: 2026-02-08]
                IconButton(
                    onClick = onAddImageClick,
                    enabled = canAddMoreImages
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Adicionar Imagem",
                        // Se estiver travado, o ícone fica mais escuro/transparente
                        tint = if (canAddMoreImages) Color.Gray else Color.DarkGray.copy(alpha = 0.5f)
                    )
                }

                // 5. MARCADOR: Pincel para cores neon e fundo de texto
                ToolbarButton(Icons.Default.Brush, "Marcador", onClick = onMarkerClick)
            }

            // BOTÃO SALVAR (Check FAB) - Canto Direito
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = accentColor, // Usa o Neon Green
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-10).dp) // Efeito visual de estar "saindo" da barra
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Salvar Nota",
                    tint = Color.Black // Contraste preto no fundo neon
                )
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
    neonGreen: Color
) {
    Surface(
        color = Color(0xFF2A2A2A),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBoldClick) {
                    Icon(
                        Icons.Default.FormatBold,
                        null,
                        tint = if (isBoldActive) neonGreen else Color.White
                    )
                }
                IconButton(onClick = onItalicClick) {
                    Icon(
                        Icons.Default.FormatItalic,
                        null,
                        tint = Color.White
                    )
                }
                IconButton(onClick = onUnderlineClick) {
                    Icon(
                        Icons.Default.FormatUnderlined,
                        null,
                        tint = Color.White
                    )
                }
                IconButton(onClick = onBulletClick) {
                    Icon(
                        Icons.Default.FormatListBulleted,
                        null,
                        tint = neonGreen
                    )
                }
                IconButton(onClick = onQuoteClick) {
                    Icon(
                        Icons.Default.FormatQuote,
                        null,
                        tint = neonGreen
                    )
                }
            }
            Divider(
                color = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Tam:",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
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
    val colors =
        listOf(neonGreen, Color(0xFFFF00E5), Color(0xFF00B2FF), Color(0xFFE6FB04), Color.White)
    Surface(
        color = Color(0xFF2A2A2A),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Cor do Texto", color = Color.Gray, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .background(color, CircleShape)
                            .clickable { onTextColorSelected(color) })
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Marcador (Fundo)", color = Color.Gray, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .border(1.dp, color, CircleShape)
                            .background(color.copy(alpha = 0.3f), CircleShape)
                            .clickable { onMarkerColorSelected(color) })
                }
            }
        }
    }
}

@Composable
fun FloatingEmojiMenu(
    onEmojiSelected: (String, String) -> Unit, // Retorna o emoji e o nome do humor (ex: "😊", "Feliz")
    neonGreen: Color                           // Cor de destaque para o estilo do app
) {
    // 1. LISTA OFICIAL DE SENTIMENTOS [cite: 2026-02-08]
    val diaryEmojis = listOf(
        "😭" to "Muito Triste",
        "😢" to "Triste",
        "😐" to "Neutro",
        "😊" to "Feliz",
        "🤩" to "Muito Feliz",
        "😫" to "Estressado",
        "😡" to "Bravo"
    )

    // 2. CONTAINER DO MENU FLUTUANTE
    Surface(
        color = Color(0xFF2A2A2A), // Cinza escuro para destacar do fundo preto
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .border(1.dp, neonGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp)) // Borda neon suave
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Título interno do menu
            Text(
                text = "Como você está se sentindo?",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            // 3. LINHA DE EMOJIS COM ROLAGEM HORIZONTAL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()), // Permite deslizar os 7 emojis
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                diaryEmojis.forEach { (emoji, humor) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onEmojiSelected(emoji, humor) } // Atualiza o topo da nota [cite: 2026-02-08]
                            .padding(8.dp)
                    ) {
                        Text(text = emoji, fontSize = 32.sp) // Emoji grande para facilitar o toque
                        Text(
                            text = humor,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayerComponent(accentColor: Color) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
            Text(
                "0:00 / 0:30",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            LinearProgressIndicator(
                progress = 0.3f,
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = accentColor,
                trackColor = Color.DarkGray
            )
        }
    }
}