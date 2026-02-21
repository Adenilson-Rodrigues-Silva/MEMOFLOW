package com.example.memoflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteNoteScreen(onBack: () -> Unit) {
    val neonGreen = Color(0xFF00FFC2)
    val surfaceDark = Color(0xFF1E1E1E)

    // Estados da nota
    var title by remember { mutableStateOf("Hoje") }
    var content by remember { mutableStateOf("Escrevendo minha primeira memória...") }
    var selectedEmoji by remember { mutableStateOf("😊") }
    var selectedHumor by remember { mutableStateOf("Feliz") }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Entrada", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
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
            NoteBottomToolbar(neonGreen)
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
                text = "11 DE FEVEREIRO DE 2024",
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
                                value = title,
                                onValueChange = { title = it },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(neonGreen)
                            )
                            Text("Humor: $selectedHumor", color = neonGreen, fontSize = 14.sp)
                        }
                        Text(selectedEmoji, fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    BasicTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp),
                        textStyle = TextStyle(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(neonGreen)
                    )

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
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("0:00", color = Color.White, fontSize = 12.sp)

            Box(modifier = Modifier.weight(1f).height(40.dp).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(20) { index ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((10..30).random().dp)
                                .background(if (index < 5) accentColor else Color.Gray.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                }
            }
            Text("0:30", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun NoteBottomToolbar(accentColor: Color) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolbarButton(Icons.Default.Mic, "Voz")
                ToolbarButton(Icons.Default.Image, "Imagem")
                ToolbarButton(Icons.Default.Face, "Emoji")
                ToolbarButton(Icons.Default.FormatBold, "Negrito")
            }

            FloatingActionButton(
                onClick = { /* Salvar Nota */ },
                containerColor = accentColor,
                shape = CircleShape,
                modifier = Modifier.size(56.dp).offset(y = (-20).dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black)
            }
        }
    }
}

@Composable
fun ToolbarButton(icon: ImageVector, desc: String) {
    Icon(
        imageVector = icon,
        contentDescription = desc,
        tint = Color.Gray,
        modifier = Modifier.size(26.dp).clickable { /* Ação */ }
    )
}

@Preview
@Composable
fun WriteNoteScreenPreview() {
    WriteNoteScreen(onBack = {})
}