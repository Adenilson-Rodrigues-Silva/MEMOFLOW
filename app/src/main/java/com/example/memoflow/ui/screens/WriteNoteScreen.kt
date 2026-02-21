package com.example.memoflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.memoflow.viewmodel.WriteNoteViewModel
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteNoteScreen(
    onBack: () -> Unit,
    viewModel: WriteNoteViewModel = viewModel()
) {
    val state = viewModel.uiState
    val richTextState = viewModel.richTextState

    val neonGreen = Color(0xFF00FFC2)
    val surfaceDark = Color(0xFF1E1E1E)

    // Estados de interface (Menus)
    var showFormatMenu by remember { mutableStateOf(false) }
    var showEmojiMenu by remember { mutableStateOf(false) }
    var showMarkerMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Entrada", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(32.dp))
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
                AnimatedVisibility(visible = showFormatMenu) {
                    FloatingFormatMenu(
                        onBoldClick = { viewModel.toggleBold() },
                        onItalicClick = { viewModel.toggleItalic() },
                        onUnderlineClick = { viewModel.toggleUnderline() },
                        // A biblioteca já nos diz se o estilo está ativo:
                        isBoldActive = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                        isItalicActive = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic,
                        isUnderlineActive = richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
                        neonGreen = neonGreen
                    )
                }

                // Menu de Marcadores (Marker)
                AnimatedVisibility(visible = showMarkerMenu) {
                    FloatingMarkerMenu(
                        onColorSelected = { color ->
                            viewModel.applyMarker(color)
                            showMarkerMenu = false
                        },
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
                                textStyle = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold),
                                cursorBrush = SolidColor(neonGreen)
                            )
                            Text("Humor: ${state.selectedHumor}", color = neonGreen, fontSize = 14.sp)
                        }
                        Text(state.selectedEmoji, fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // O EDITOR DE TEXTO RICO (Onde a mágica acontece)
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

                    // --- SLOT PARA AS 3 IMAGENS (Pilar do Projeto) ---
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
                                    .clickable { /* Futura Galeria */ },
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUri != null) {
                                    Text("📸", fontSize = 20.sp) // Representação da foto
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
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun NoteBottomToolbar(
    accentColor: Color,
    onFormatClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onMarkerClick: () -> Unit
) {
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
                ToolbarButton(Icons.Default.Mic, "Voz") { /* Audio 30s */ }
                ToolbarButton(Icons.Default.FormatBold, "Formatar") { onFormatClick() }
                ToolbarButton(Icons.Default.Face, "Emoji") { onEmojiClick() }
                ToolbarButton(Icons.Default.Image, "Imagem") { /* Add Imagem */ }
                ToolbarButton(Icons.Default.Brush, "Marcador") { onMarkerClick() }
            }
            FloatingActionButton(
                onClick = { /* Save Room */ },
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
    onBoldClick: () -> Unit, onItalicClick: () -> Unit, onUnderlineClick: () -> Unit,
    isBoldActive: Boolean, isItalicActive: Boolean, isUnderlineActive: Boolean, neonGreen: Color
) {
    Surface(color = Color(0xFF2A2A2A), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBoldClick) { Icon(Icons.Default.FormatBold, null, tint = if(isBoldActive) neonGreen else Color.White) }
            IconButton(onClick = onItalicClick) { Icon(Icons.Default.FormatItalic, null, tint = if(isItalicActive) neonGreen else Color.White) }
            IconButton(onClick = onUnderlineClick) { Icon(Icons.Default.FormatUnderlined, null, tint = if(isUnderlineActive) neonGreen else Color.White) }
        }
    }
}

@Composable
fun FloatingMarkerMenu(onColorSelected: (Color) -> Unit, neonGreen: Color) {
    val colors = listOf(neonGreen, Color(0xFFFF00E5), Color(0xFF00B2FF), Color(0xFFE6FB04))
    Surface(color = Color(0xFF2A2A2A), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier.size(30.dp).background(color.copy(alpha = 0.4f), CircleShape)
                        .border(2.dp, color, CircleShape).clickable { onColorSelected(color.copy(alpha = 0.3f)) }
                )
            }
        }
    }
}

@Composable
fun FloatingEmojiMenu(onEmojiSelected: (String, String) -> Unit, neonGreen: Color) {
    val emojis = listOf("😊" to "Feliz", "😭" to "Triste", "😍" to "Apaixonado", "😎" to "Confiante", "🤔" to "Pensativo")
    Surface(color = Color(0xFF2A2A2A), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.padding(8.dp).horizontalScroll(rememberScrollState())) {
            emojis.forEach { (emoji, humor) ->
                Text(emoji, modifier = Modifier.clickable { onEmojiSelected(emoji, humor) }.padding(8.dp), fontSize = 28.sp)
            }
        }
    }
}

@Composable
fun AudioPlayerComponent(accentColor: Color) {
    Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
            Text("0:00 / 0:30", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
            LinearProgressIndicator(progress = 0.3f, modifier = Modifier.weight(1f).height(4.dp), color = accentColor, trackColor = Color.DarkGray)
        }
    }
}