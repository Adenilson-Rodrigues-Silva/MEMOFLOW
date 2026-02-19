package com.example.memoflow.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memoflow.ui.components.*
import com.example.memoflow.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.memoflow.R

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {

    val neonGreen = Color(0xFF00FFC2)
    val isMenuExpanded by viewModel.isMenuExpanded.collectAsState()

    val rotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 135f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
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
            HomeContent(padding, neonGreen)
        }
    }
}

@Composable
fun HomeContent(padding: PaddingValues, neonGreen: Color) {
    // 1. DATA E FORMATAÇÃO
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
    val formattedDate = currentDate.format(formatter)
        .replaceFirstChar { it.uppercase() }

    // 2. GERAÇÃO DE 31 DIAS (Para o scroll funcionar de verdade)
    // Criamos uma lista de LocalDate para ser mais preciso
    val daysList = remember {
        (0..30).map { currentDate.minusDays(it.toLong()) }
    }

    // Estado da data selecionada (começa com hoje)
    var selectedDate by remember { mutableStateOf(currentDate) }

    // 3. CONFIGURAÇÃO DO LOTTIE (layout_vazio.json)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.layout_vazio))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color.Black) // Garante o fundo preto do design
    ) {
        // Cabeçalho com a data formatada
        HomeHeader(formattedDate)

        Spacer(modifier = Modifier.height(16.dp))

        // Calendário Row com a lista de 31 dias
        // Nota: Ajuste seu CalendarRow para receber List<LocalDate> e LocalDate
        CalendarRow(
            days = daysList.map { it.dayOfMonth.toString() }, // Convertendo para String se seu componente pedir String
            selectedDay = selectedDate.dayOfMonth.toString(),
            neonGreen = neonGreen
        ) { dayString ->
            // Encontra a data correspondente ao dia clicado para atualizar o estado
            selectedDate = daysList.find { it.dayOfMonth.toString() == dayString } ?: currentDate
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. ÁREA DA ANIMAÇÃO (Substituindo o EmptyState)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(280.dp) // Tamanho ajustado para o deserto
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nenhuma memória no vácuo deste dia...",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Espaço para não ficar colado nos botões de baixo
        Spacer(modifier = Modifier.height(80.dp))
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
                HubButton(Icons.Default.Add, "Novo", neonGreen) {}
                HubButton(Icons.Default.Edit, "Gráficos", neonGreen) {}
                HubButton(Icons.Default.Edit, "Gráficos", neonGreen) {}
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


