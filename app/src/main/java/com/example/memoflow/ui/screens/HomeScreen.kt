package com.example.memoflow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
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
    val isMenuExpanded by viewModel.isMenuExpanded.collectAsState()

    val rotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 135f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "FabRotation"
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
                    isMenuExpanded = isMenuExpanded,
                    rotation = rotation,
                    neonGreen = neonGreen,
                    onToggle = { viewModel.toggleMenu() }
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
    isMenuExpanded: Boolean,
    rotation: Float,
    neonGreen: Color,
    onToggle: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Adicionei o ícone de microfone aqui para combinar com o projeto
                HubButton(Icons.Default.Add, "Voz", neonGreen) { /* Ação Áudio */ }
                HubButton(Icons.Default.Edit, "Texto", neonGreen) { /* Ação Texto */ }
                HubButton(Icons.Default.Add, "Novo", neonGreen) { /* Ação Geral */ }
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            containerColor = neonGreen,
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer(rotationZ = rotation)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.Black
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