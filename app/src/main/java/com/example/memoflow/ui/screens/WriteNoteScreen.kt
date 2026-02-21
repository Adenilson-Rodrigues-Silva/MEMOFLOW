package com.example.memoflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memoflow.viewmodel.WriteNoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteNoteScreen(
    onBack: () -> Unit,
    viewModel: WriteNoteViewModel = viewModel()
) {


    val state = viewModel.uiState

    val neonGreen = Color(0xFF00FFC2)
    val surfaceDark = Color(0xFF1E1E1E)


    var selectedEmoji by remember { mutableStateOf("😊") }


    // Estados para os menus flutuantes da barra inferior
    var showFormatMenu by remember { mutableStateOf(false) }
    var showEmojiMenu by remember { mutableStateOf(false) }
    // No topo da WriteNoteScreen, junto com os outros 'var'

    var showMarkerMenu by remember { mutableStateOf(false) } // Adicione esta linha!

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Entrada", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu de opções */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            Column {
                // Menu de Formatação
                AnimatedVisibility(visible = showFormatMenu) {
                    FloatingFormatMenu( // Sugiro um componente específico ou adaptar o seu
                        onBoldClick = { viewModel.toggleBold() },
                        onItalicClick = { viewModel.toggleItalic() },
                        onUnderlineClick = { viewModel.toggleUnderline() },
                        isBoldActive = state.isBold,
                        isItalicActive = state.isItalic,
                        isUnderlineActive = state.isUnderline,
                        neonGreen = neonGreen
                    )
                }

                // Menu de Emojis
                AnimatedVisibility(visible = showEmojiMenu) {
                    FloatingEmojiMenu(
                        onEmojiSelected = { emoji, humor ->
                            viewModel.updateEmoji(emoji, humor)
                            showEmojiMenu = false // Fecha ao selecionar
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
                    // Não esqueça de passar os outros cliques (foto, áudio, marcador)
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
                text = "11 DE FEVEREIRO DE 2024", // Pode ser dinâmico depois
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- CARD PRINCIPAL ---
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
                            BasicTextField(
                                value = state.title, // VEM DO WRITE NOTE VIEW MODEL
                                onValueChange = { novoTitulo -> // 3. Nomeamos de 'novoTitulo' para evitar erro no 'it'
                                    viewModel.updateTitle(novoTitulo)
                                },
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
                        Text(selectedEmoji, fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    BasicTextField(
                        value = state.content,
                        onValueChange = { newValue: TextFieldValue ->
                            viewModel.updateContent(newValue)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        enabled = true,
                        readOnly = false,
                        textStyle = TextStyle( // APENAS UM BLOCO DESTE
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(neonGreen)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- SLOT PARA AS 3 IMAGENS ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = "Imagem",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AudioPlayerComponent(neonGreen)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Dono do Chronos",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 20.sp,
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

// COMPONENTE NOVO: O Menu que "abre para cima"
@Composable
fun FloatingMenu(items: List<Pair<String, String>>, neonGreen: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color(0xFF2A2A2A),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.wrapContentWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    Text(
                        text = item.first,
                        color = if (item.first == "B" || item.first == "I" || item.first == "U") neonGreen else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* Ação do item */ }
                    )
                }
            }
        }
    }
}

@Composable
fun AudioPlayerComponent(accentColor: Color) {
    Surface(
        color = Color.Black.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("0:00", color = Color.White, fontSize = 12.sp)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(20) { index ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((10..30).random().dp)
                                .background(
                                    if (index < 5) accentColor else Color.Gray.copy(alpha = 0.3f),
                                    CircleShape
                                )
                        )
                    }
                }
            }
            Text("0:30", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun NoteBottomToolbar(accentColor: Color, onFormatClick: () -> Unit, onEmojiClick: () -> Unit) {
    Surface(
        color = Color(0xFF121212),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Grupo de Formatação Ajustado
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolbarButton(Icons.Default.Mic, "Voz") { /* Iniciar/Parar Áudio 30s */ }
                ToolbarButton(Icons.Default.FormatBold, "Formatar") { onFormatClick() }
                ToolbarButton(Icons.Default.Face, "Emoji") { onEmojiClick() }
                ToolbarButton(Icons.Default.Image, "Imagem") { /* Add Imagem */ }
                ToolbarButton(Icons.Default.Brush, "Marcador") { /* Marcador de texto */ }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Botão Principal de Salvar (Lápis)
            FloatingActionButton(
                onClick = { /* Salvar Nota no Room */ },
                containerColor = accentColor,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-10).dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black)
            }
        }
    }
}

@Composable
fun ToolbarButton(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Surface(
        color = Color.Transparent,
        shape = CircleShape,
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = Color.Gray,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun FloatingFormatMenu(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    isBoldActive: Boolean,
    isItalicActive: Boolean,
    isUnderlineActive: Boolean,
    neonGreen: Color
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color(0xFF2A2A2A),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botão Negrito
            IconButton(onClick = onBoldClick) {
                Icon(
                    imageVector = Icons.Default.FormatBold,
                    contentDescription = "Negrito",
                    tint = if (isBoldActive) neonGreen else Color.White
                )
            }
            // Botão Itálico
            IconButton(onClick = onItalicClick) {
                Icon(
                    imageVector = Icons.Default.FormatItalic,
                    contentDescription = "Itálico",
                    tint = if (isItalicActive) neonGreen else Color.White
                )
            }
            // Botão Sublinhado
            IconButton(onClick = onUnderlineClick) {
                Icon(
                    imageVector = Icons.Default.FormatUnderlined,
                    contentDescription = "Sublinhado",
                    tint = if (isUnderlineActive) neonGreen else Color.White
                )
            }
        }
    }
}

@Composable
fun FloatingEmojiMenu(
    onEmojiSelected: (String, String) -> Unit,
    neonGreen: Color
) {
    // Lista de Emojis e seus respectivos Humores
    val emojiList = listOf(
        "😊" to "Feliz",
        "😭" to "Triste",
        "😍" to "Apaixonado",
        "😡" to "Irritado",
        "😎" to "Confiante",
        "😴" to "Cansado",
        "🤔" to "Pensativo"
    )

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color(0xFF2A2A2A),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .horizontalScroll(rememberScrollState()), // Permite rolar se tiver muitos
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            emojiList.forEach { (emoji, humor) ->
                Text(
                    text = emoji,
                    modifier = Modifier
                        .clickable { onEmojiSelected(emoji, humor) }
                        .padding(8.dp),
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun WriteNoteScreenPreview() {
    WriteNoteScreen(onBack = {})
}