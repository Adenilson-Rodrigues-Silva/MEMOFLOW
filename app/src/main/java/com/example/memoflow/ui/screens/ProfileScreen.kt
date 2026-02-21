package com.example.memoflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.layout.ContentScale


@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSecurityClick: () -> Unit,
    onBackupClick: () -> Unit
) {
    val neonGreen = Color(0xFF00FFC2)
    var userBio by remember { mutableStateOf("Escrevendo para não esquecer quem eu era.") }

    // ESTRATÉGICO: Variável que guarda o endereço da foto escolhida
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // ESTRATÉGICO: O "Contrato" que abre a galeria do Android
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Quando o usuário escolhe a foto, o endereço (uri) cai aqui
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Toolbar superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Text(
                "Configurações",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Foto de Perfil Grande
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            // ESTRATÉGICO: Clicar na bola também abre a galeria
                            .clickable { galleryLauncher.launch("image/*") },
                        shape = CircleShape,
                        color = neonGreen
                    ) {
                        // ESTRATÉGICO: Lógica de exibição da imagem
                        if (selectedImageUri == null) {
                            // Se não houver foto, mostra o ícone de pessoa
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(25.dp),
                                tint = Color.Black
                            )
                        } else {
                            // Se houver foto, o Coil (AsyncImage) carrega ela
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop // Garante que a foto preencha o círculo
                            )
                        }
                    }

                    // Botão flutuante para editar a foto (Lápis)
                    SmallFloatingActionButton(
                        // ESTRATÉGICO: Abre a galeria ao clicar no lápis
                        onClick = { galleryLauncher.launch("image/*") },
                        containerColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Dono do Chronos",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                // Campo de Bio (Frase de Inspiração)
                Text(
                    text = userBio,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 4.dp)
                        .clickable { /* TODO: Abrir Dialog para editar Bio */ }
                )

                Text(
                    "membro desde fev 2026",
                    color = neonGreen.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(40.dp))
            }

            // --- SEÇÕES DE CONFIGURAÇÃO ---

            item {
                SectionTitle("Privacidade")
                ProfileOptionItem(
                    title = "Segurança",
                    subtitle = "Biometria e PIN",
                    icon = Icons.Default.Lock,
                    onClick = { onSecurityClick() } // chama a função aqui
                )
            }

            item {
                SectionTitle("Personalização")
                ProfileOptionItem(
                    title = "Aparência",
                    subtitle = "Tema e Cores",
                    icon = Icons.Default.Palette,
                    onClick = { /* TODO: Abrir BottomSheet de Temas */ }
                )
                ProfileOptionItem(
                    title = "Lembrete Diário",
                    subtitle = "Definir horário de notificação",
                    icon = Icons.Default.Notifications,
                    onClick = { /* TODO: Abrir TimePicker */ }
                )
            }

            item {
                SectionTitle("Dados")
                ProfileOptionItem(
                    title = "Backup",
                    subtitle = "Nuvem e Exportação PDF",
                    icon = Icons.Default.CloudUpload,
                    onClick = { onBackupClick() } // <-- 2. CONECTE O CLIQUE AQUI PARA A TELA DE EM CONSTRUÇÃO
                )
                ProfileOptionItem(
                    title = "Limpar Diário",
                    subtitle = "Apagar todas as memórias",
                    icon = Icons.Default.DeleteForever,
                    color = Color.Red.copy(alpha = 0.8f),
                    onClick = { /* TODO: Abrir Dialog de Confirmação */ }
                )
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

// COMPONENTES AUXILIARES (Sem alteração)

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 16.dp)
    )
}


@Composable
fun ProfileOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícone com fundo sutil
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(title, color = color, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight, // Use o nome oficial direto aqui
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}


@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun ProfileScreenPreview() {
    // Usamos o tema do seu projeto para as cores ficarem certas
    com.example.memoflow.ui.theme.MemoFlowTheme(darkTheme = true) {
        ProfileScreen(
            onBack = { },
            onSecurityClick = { },
            onBackupClick = { } )// <-- ADICIONE ISSO)

    }
}