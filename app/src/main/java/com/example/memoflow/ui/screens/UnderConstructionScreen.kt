package com.example.memoflow.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.memoflow.R

@Composable
fun UnderConstructionScreen(onBack: () -> Unit) {
    // Definição da cor Neon principal do app
    val neonGreen = Color(0xFF00FFC2)

    // --- LÓGICA DA ANIMAÇÃO LOTTIE ---
    // 1. Busca o arquivo JSON dentro da pasta res/raw
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.under_construction_2))

    // 2. Define as configurações da animação (como o loop infinito)
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // Layout principal em Coluna (fundo preto)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // --- CABEÇALHO (Botão de Voltar) ---
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        // --- CORPO DA TELA (Conteúdo Centralizado) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Empurra o conteúdo um pouco para cima
            verticalArrangement = Arrangement.Center, // Centraliza na vertical
            horizontalAlignment = Alignment.CenterHorizontally // Centraliza na horizontal
        ) {

            // Exibição da Animação Lottie
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(300.dp) // Define o tamanho do desenho
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Título Principal
            Text(
                text = "MÓDULO EM\nCONSTRUÇÃO",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            // Texto de Descrição
            Text(
                text = "O Backup em nuvem estará disponível na versão 2.0 do Chronos Diary.",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de ação para retornar
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "ENTENDI",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
fun ConstructionPreview() {
    UnderConstructionScreen(onBack = {})
}