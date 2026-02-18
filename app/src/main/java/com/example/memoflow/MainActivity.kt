package com.example.memoflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memoflow.ui.theme.MemoFlowTheme
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemoFlowTheme {
                var currentScreen by remember { mutableStateOf("splash") }
                if (currentScreen == "splash") {
                    SplashScreen { currentScreen = "home" }
                } else {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(1200))
        delay(1800)
        onFinished()
    }
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Text("MEMOFLOW", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp, modifier = Modifier.alpha(alphaAnim.value))
    }
}

@Composable
fun HomeScreen() {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var explosionState by remember { mutableStateOf(false) }
    val neonGreen = Color(0xFF00FFC2)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // HUB DE 3 BOTÕES (SALTANDO COM SPRING)
                    AnimatedVisibility(
                        visible = isMenuExpanded,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }) + scaleIn(),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }) + scaleOut()
                    ) {
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            HubButton(Icons.Default.Add, "Galeria", neonGreen)
                            HubButton(Icons.Default.Add, "Voz", neonGreen)
                            HubButton(Icons.Default.Edit, "Nota", neonGreen)
                        }
                    }

                    // BOTÃO PRINCIPAL + COM GLOW
                    FloatingActionButton(
                        onClick = {
                            isMenuExpanded = !isMenuExpanded
                            explosionState = true
                        },
                        containerColor = neonGreen,
                        shape = CircleShape,
                        modifier = Modifier.size(68.dp).neonGlow(neonGreen)
                    ) {
                        Icon(
                            imageVector = if (isMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)) {
                // HEADER ESTILIZADO
                Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Meu Diário", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                        Text("Segunda-feira, 16 de fevereiro", color = Color.Gray, fontSize = 15.sp)
                    }
                    Box(Modifier.size(50.dp).clip(CircleShape).background(neonGreen)) // Perfil do Print
                }

                Spacer(Modifier.height(30.dp))

                // CALENDÁRIO IGUAL AO PRINT
                val dias = listOf("16", "15", "14", "13", "12", "11")
                val semanas = listOf("SEG.", "DOM.", "SÁB.", "SEX.", "QUI.", "QUA.")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    items(dias.size) { i ->
                        val isToday = i == 0
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(semanas[i], color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Box(
                                Modifier.size(48.dp).clip(CircleShape)
                                    .background(if (isToday) Color.Transparent else Color(0xFF1A1A1A))
                                    .border(2.dp, if (isToday) neonGreen else Color.Transparent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dias[i], color = if (isToday) neonGreen else Color.White, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // ÁREA CENTRAL COM LOTTIE
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.layout_vazio))
                    LottieAnimation(composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(280.dp))
                }

                // CARD HUMOR COM BORDA REFINADA
                Card(
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
                    shape = RoundedCornerShape(32.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222222))
                ) {
                    Text("Humor da Semana", Modifier.padding(20.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        // EXPLOSÃO DE PIXELS
        if (explosionState) {
            PixelExplosion(atX = 900f, atY = 1850f) { explosionState = false }
            explosionState = false
        }
    }
}

@Composable
fun HubButton(icon: ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            style = LocalTextStyle.current.copy(shadow = Shadow(color = color, blurRadius = 15f)))
        SmallFloatingActionButton(
            onClick = { },
            containerColor = Color(0xFF121212),
            contentColor = color,
            shape = CircleShape,
            modifier = Modifier.size(54.dp).border(1.5.dp, color.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(icon, null, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
fun PixelExplosion(atX: Float, atY: Float, onFinished: () -> Unit) {
    // Criamos uma lista de estados para cada partícula (X, Y e Alpha)
    val particleCount = 30
    val animatables = remember { List(particleCount) { Animatable(0f) } }

    // Dispara a animação
    LaunchedEffect(Unit) {
        animatables.forEachIndexed { index, anim ->
            // Cada partícula tem um pequeno atraso para parecer mais orgânico
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, delayMillis = index * 2)
            )
        }
        onFinished() // Remove o componente da tela após a animação
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        animatables.forEachIndexed { i, anim ->
            val progress = anim.value
            if (progress < 1f) {
                // Cálculo de direção aleatória baseado no índice da partícula
                val angle = (i.toFloat() / particleCount) * 2f * Math.PI
                val velocity = 400f * (0.5f + (i % 5) / 5f)

                val offsetX = (Math.cos(angle) * velocity * progress).toFloat()
                val offsetY = (Math.sin(angle) * velocity * progress).toFloat() - (progress * 200f) // Sobe um pouco

                drawRect(
                    color = Color(0xFF00FFC2).copy(alpha = 1f - progress),
                    topLeft = Offset(atX + offsetX, atY + offsetY),
                    size = androidx.compose.ui.geometry.Size(12f, 12f)
                )
            }
        }
    }
}

fun Modifier.neonGlow(color: Color) = this.drawBehind {
    drawCircle(color = color.copy(alpha = 0.25f), radius = size.maxDimension * 0.85f, center = center)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomePreview() {
    MemoFlowTheme {
        HomeScreen() // Ele desenha a tela aqui mesmo no editor!
    }
}