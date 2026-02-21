package com.example.memoflow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.memoflow.ui.components.*
import com.example.memoflow.ui.viewmodel.HomeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.example.memoflow.navigation.Screen
import com.example.memoflow.ui.theme.MemoFlowTheme

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val neonGreen = Color(0xFF00FFC2)
    var isMenuExpanded by remember { mutableStateOf(false) }



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
                    onToggle = { isMenuExpanded = !isMenuExpanded }
                )
            }
        ) { padding ->
            // CORREÇÃO: Agora passando o navController para o conteúdo
            HomeContent(padding, neonGreen, navController)
        }
    }
}

@Composable
fun HomeContent(
    padding: PaddingValues,
    neonGreen: Color,
    navController: NavController
) {
    val currentDate = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("pt", "BR")) }

    val formattedDate = remember(currentDate) {
        currentDate.format(formatter).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(java.util.Locale("pt", "BR")) else it.toString()
        }
    }

    val daysList = remember {
        (0..30).map { currentDate.minusDays(it.toLong()) }
    }

    var selectedDate by remember { mutableStateOf(currentDate) }
    val notes = remember { mutableStateListOf<String>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = padding
    ) {
        // 1. Cabeçalho com clique para o perfil
        item {
            HomeHeader(
                date = formattedDate,
                userPhotoUrl = null,
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Calendário
        item {
            CalendarRow(
                days = daysList,
                selectedDay = selectedDate,
                neonGreen = neonGreen,
                onDaySelected = { selectedDate = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. Notas ou Cacto
        if (notes.isEmpty()) {
            item {
                EmptyStateLottie()
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            items(notes) { note ->
                DiaryNoteCard(
                    emoji = "😊",
                    time = "20/02/2026 18:30",
                    content = note,
                    neonGreen = neonGreen
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 4. Gráfico no final (UX Check ✅)
        item {
            MoodChartCard(neonGreen = neonGreen)
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun FabMenu(
    navController: androidx.navigation.NavController, // Adicionado para navegar
    isMenuExpanded: Boolean,
    rotation: Float,
    neonGreen: Color,
    onToggle: () -> Unit
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
                // 1. BOTÃO ESTATÍSTICAS
                HubButton(Icons.Default.BarChart, "Status", neonGreen) {
                    onToggle() // Fecha o menu
                    navController.navigate("construction")
                }

                // 2. BOTÃO METAS
                HubButton(Icons.Default.Flag, "Metas", neonGreen) {
                    onToggle()
                    navController.navigate("construction")
                }

                // 3. BOTÃO NOTAS (O que você quer testar)
                HubButton(Icons.Default.Edit, "Nota", neonGreen) {
                    onToggle()
                    navController.navigate("write_note") // CHAMA A TELA DE ESCRITA
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
        // CORREÇÃO: Usando um NavController de teste para o Preview não quebrar
        val previewNavController = rememberNavController()
        Scaffold(
            containerColor = Color.Black
        ) { padding ->
            HomeContent(
                padding = padding,
                neonGreen = Color(0xFF00FFC2),
                navController = previewNavController
            )
        }
    }
}