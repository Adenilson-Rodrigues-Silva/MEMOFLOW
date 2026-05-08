package com.arsdevstudio.memoflow.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.arsdevstudio.memoflow.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val lottieRes: Int,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Bem-vindo ao MemoFlow",
            description = "Suas memórias organizadas geograficamente. Siga seus rastros e eternize cada momento no mapa da sua vida.",
            lottieRes = R.raw.animation_login_dois,
            accentColor = Color(0xFF00FFC2)
        ),
        OnboardingPage(
            title = "Inteligência que te Entende",
            description = "Receba insights emocionais baseados nas suas notas e descubra padrões no seu fluxo de humor com nossa IA.",
            lottieRes = R.raw.animation_groq_dois,
            accentColor = Color(0xFF80DEEA)
        ),
        OnboardingPage(
            title = "Segurança e Nuvem",
            description = "Seus dados protegidos pela segurança do Google. Sincronize tudo e nunca perca uma lembrança preciosa.",
            lottieRes = R.raw.animation_backup_dois,
            accentColor = Color(0xFF4285F4)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    
    val currentAccentColor = pages[pagerState.currentPage].accentColor
    val animatedAccentColor by animateColorAsState(
        targetValue = currentAccentColor.copy(alpha = 0.35f),
        animationSpec = tween(1000),
        label = "accent_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camada 1: Degradê de profundidade (Azul escuro para Preto)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E293B), // Slate 800 (Mais claro no topo)
                            Color(0xFF0F172A), // Slate 900 (Meio)
                            Color.Black        // Preto (Base)
                        )
                    )
                )
        )

        // Camada 2: Brilho dinâmico que segue a cor da página
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(animatedAccentColor, Color.Transparent),
                        endY = 1400f // Faz o brilho dissipar antes de chegar no rodapé
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                OnboardingPageContent(pages[index])
            }

            // Bottom Navigation Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pages.size) { i ->
                        val color = if (pagerState.currentPage == i) pages[i].accentColor else Color.Gray.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == i) 12.dp else 8.dp)
                                .background(color, CircleShape)
                        )
                    }
                }

                // Next/Finish Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].accentColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Começar" else "Próximo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(page.lottieRes))
        val progress by animateLottieCompositionAsState(
            composition, 
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(360.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
