package com.example.memoflow.ui.components.home

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.SolidColor
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.memoflow.R
import java.util.Date
import kotlin.random.Random

// Cores Temáticas
val CyanAI = Color(0xFF00E5FF)
val PurpleAI = Color(0xFFD500F9)
val AlertRed = Color(0xFFFF1744)

@Composable
fun rememberAnimatedAiGradient(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "offset"
    )
    
    return Brush.linearGradient(
        colors = listOf(CyanAI, PurpleAI, CyanAI),
        start = Offset(offset, offset),
        end = Offset(offset + 1000f, offset + 1000f),
        tileMode = TileMode.Repeated
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiaryNoteCard(
    emoji: String, 
    time: String, 
    title: String, 
    content: String, 
    neonGreen: Color,
    isLocked: Boolean = false,
    isTimeCapsule: Boolean = false,
    unlockDate: Long? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val cyan = Color(0xFF00E5FF)
    val iceBlue = Color(0xFFB3E5FC)
    val deepBlue = Color(0xFF01579B)
    
    // Lógica da Cápsula
    val isReadyToMelt = isTimeCapsule && (unlockDate != null && unlockDate <= System.currentTimeMillis())

    val infiniteTransition = rememberInfiniteTransition(label = "card_effects")
    
    // --- Animações para "Memória Trancada" ---
    val scannerPosition by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearOutSlowInEasing), RepeatMode.Reverse), label = "scanner"
    )
    
    // Noise Toggle mais rápido para o efeito de "Neve" (Static Noise)
    val noiseToggle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(50, easing = LinearEasing), RepeatMode.Restart), label = "noise"
    )
    
    val borderOffsetLocked by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 500f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "border_locked"
    )
    
    val flickerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                0.95f at 0
                0.80f at 100
                1.0f at 150
                0.90f at 400
                0.75f at 500
                1.0f at 600
            },
            repeatMode = RepeatMode.Restart
        ), label = "flicker"
    )

    // --- Animações para Cápsula (Shake Ajustado) ---
    // Usamos keyframes para alternar entre vibrar e ficar parado, com menos intensidade
    val shakeAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f, // Valor de controle
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0 // Parado
                0f at 1500 // Continua parado por 1.5s
                -0.8f at 1600 // Pequena vibração
                0.8f at 1700
                -0.8f at 1800
                0.8f at 1900
                0f at 2000 // Para novamente
                0f at 3000
            },
            repeatMode = RepeatMode.Restart
        ), label = "shake"
    )

    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "sparkle"
    )

    // --- Shimmer Geral ---
    val shimmerValue by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart), label = "shimmer"
    )

    val borderBrush = when {
        isLocked -> Brush.verticalGradient(
            colors = listOf(PurpleAI, AlertRed, PurpleAI),
            startY = borderOffsetLocked,
            endY = borderOffsetLocked + 600f,
            tileMode = TileMode.Repeated
        )
        isReadyToMelt -> Brush.linearGradient(
            listOf(cyan, Color.White, Color(0xFF84FFFF), Color.White, cyan),
            start = Offset(shimmerValue, shimmerValue),
            end = Offset(shimmerValue + 500f, shimmerValue + 500f),
            tileMode = TileMode.Repeated
        )
        isTimeCapsule -> Brush.linearGradient(listOf(cyan, iceBlue, deepBlue, cyan))
        else -> {
            Brush.linearGradient(
                colors = listOf(Color(0xFF424242), Color(0xFFBDBDBD), Color.White, Color(0xFFE0E0E0), Color(0xFF424242)),
                start = Offset(shimmerValue, shimmerValue),
                end = Offset(shimmerValue + 400f, shimmerValue + 400f),
                tileMode = TileMode.Repeated
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(95.dp)
            .graphicsLayer {
                if (isReadyToMelt) {
                    rotationZ = shakeAnim
                    translationX = shakeAnim * 1f // Intensidade reduzida
                }
            }
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                onClick = {
                    if (isReadyToMelt) {
                        try {
                            val mp = MediaPlayer.create(context, R.raw.ice_sound)
                            mp.start()
                            mp.setOnCompletionListener { it.release() }
                        } catch (e: Exception) { e.printStackTrace() }

                        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        }
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    onClick()
                }, 
                onLongClick = onLongClick
            )
            .border(
                width = if (isTimeCapsule || isLocked) 2.dp else 1.2.dp, 
                brush = borderBrush, 
                shape = RoundedCornerShape(24.dp)
            )
            .background(if (isLocked) Color(0xFF080808) else Color(0xFF161616).copy(alpha = 0.85f))
    ) {
        // --- Efeitos Visuais para Memória Trancada ---
        if (isLocked) {
            // Efeito de Estática de TV (Snow) + Glitch
            Canvas(modifier = Modifier.fillMaxSize()) {
                val random = Random(noiseToggle.toBits())
                
                // 1. Ruído de "Neve" (Pontos brancos e cinzas aleatórios e rápidos)
                repeat(400) {
                    val grey = random.nextFloat()
                    val alpha = random.nextFloat() * 0.25f
                    drawRect(
                        color = Color(grey, grey, grey, alpha),
                        topLeft = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height),
                        size = Size(1.5.dp.toPx(), 1.5.dp.toPx())
                    )
                }

                // 2. Linhas de Scan Verticais/Horizontais de TV Analógica
                val lineSpacing = 4.dp.toPx()
                for (y in 0 until size.height.toInt() step lineSpacing.toInt()) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.35f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 3. Glitch Horizontal (Faixas de interferência que aparecem do nada)
                if (random.nextFloat() > 0.65f) {
                    val glitchY = random.nextFloat() * size.height
                    val glitchHeight = (random.nextFloat() * 6.dp.toPx()) + 2.dp.toPx()
                    drawRect(
                        color = Color.White.copy(alpha = 0.1f),
                        topLeft = Offset(0f, glitchY),
                        size = Size(size.width, glitchHeight)
                    )
                }
                
                // 4. "Distorsão" de cor ocasional (Roxo/Vermelho no chiado)
                if (random.nextFloat() > 0.90f) {
                    repeat(10) {
                        drawCircle(
                            color = if (random.nextBoolean()) PurpleAI.copy(alpha = 0.2f) else AlertRed.copy(alpha = 0.2f),
                            radius = 2.dp.toPx(),
                            center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
                        )
                    }
                }
            }

            // Animação de Scanner de Segurança (Luz laser)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scanY = size.height * scannerPosition
                val scannerBrush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, AlertRed.copy(alpha = 0.45f), AlertRed.copy(alpha = 0.1f), Color.Transparent),
                    startY = scanY - 18.dp.toPx(),
                    endY = scanY + 18.dp.toPx()
                )
                drawRect(brush = scannerBrush, topLeft = Offset(0f, scanY - 18.dp.toPx()), size = Size(size.width, 36.dp.toPx()))
                drawLine(color = AlertRed.copy(alpha = 0.85f), start = Offset(0f, scanY), end = Offset(size.width, scanY), strokeWidth = 1.5.dp.toPx())
            }
        }

        // --- Efeitos Visuais para Cápsula ---
        if (isTimeCapsule && isReadyToMelt) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val random = Random(42)
                val spacing = 40.dp.toPx()
                for (i in -10..10) {
                    val startX = i * spacing + (shimmerValue / 1000f * spacing)
                    drawLine(
                        color = cyan.copy(alpha = 0.15f),
                        start = Offset(startX, 0f),
                        end = Offset(startX - size.height, size.height),
                        strokeWidth = 15.dp.toPx()
                    )
                }
                repeat(12) {
                    drawCircle(
                        color = Color.White.copy(alpha = sparkleAlpha),
                        radius = 1.5.dp.toPx(),
                        center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
                    )
                }
            }
        }

        // --- Conteúdo Principal (Ícone e Textos) ---
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .graphicsLayer { 
                    if (isLocked) {
                        alpha = flickerAlpha // Aplica o efeito de "instabilidade" da TV
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        when {
                            isLocked -> PurpleAI.copy(alpha = 0.1f)
                            isTimeCapsule -> cyan.copy(alpha = 0.15f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }, 
                        CircleShape
                    )
                    .border(
                        1.5.dp, 
                        when {
                            isLocked -> PurpleAI.copy(alpha = 0.4f)
                            isTimeCapsule -> cyan.copy(alpha = 0.6f)
                            else -> Color.Gray.copy(alpha = 0.4f)
                        }, 
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLocked -> Icon(Icons.Default.Lock, null, tint = AlertRed, modifier = Modifier.size(26.dp))
                    isTimeCapsule -> Icon(if (isReadyToMelt) Icons.Default.Whatshot else Icons.Default.AcUnit, null, tint = if (isReadyToMelt) Color.White else cyan, modifier = Modifier.size(26.dp))
                    else -> Text(text = emoji, fontSize = 26.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = time, color = Color.Gray, fontSize = 11.sp)
                Text(
                    text = if (isLocked) "Memória Trancada" else if (isReadyToMelt) "PRONTA PARA ABRIR!" else if (isTimeCapsule) "Cápsula do Tempo" else title,
                    color = if (isLocked) PurpleAI else if (isReadyToMelt) Color.White else if (isTimeCapsule) cyan else Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = if (isLocked) "Segredo protegido por PIN" else if (isReadyToMelt) "Toque para derreter o gelo ✨" else if (isTimeCapsule) "Disponível em ${unlockDate?.let { java.text.SimpleDateFormat("dd/MM/yyyy").format(Date(it)) }}" else content,
                    color = Color.White.copy(alpha = if (isLocked || isReadyToMelt) 0.8f else 0.6f),
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HomeHeader(
    date: String,
    userPhotoUrl: String?,
    onProfileClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onStoreClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "diamond_glow")
    
    // Animação do brilho que "passa" (shimmer sweep)
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shine_offset"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Meu Diário", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text(date, color = Color(0xFFAAAAAA), fontSize = 14.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onStoreClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(PurpleAI.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, PurpleAI.copy(alpha = 0.3f), CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Diamond, 
                        contentDescription = "Loja", 
                        tint = PurpleAI, 
                        modifier = Modifier.size(24.dp)
                    )
                    
                    // Camada de Brilho Extra (Sweep)
                    Canvas(modifier = Modifier.size(24.dp).clip(CircleShape)) {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.6f),
                                    Color.Transparent
                                ),
                                start = Offset(shineOffset, 0f),
                                end = Offset(shineOffset + 30.dp.toPx(), size.height)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onCalendarClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.Default.DateRange, null, tint = Color(0xFF00FFC2), modifier = Modifier.size(22.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() },
                shape = CircleShape,
                color = Color(0xFF00FFC2) 
            ) {
                if (userPhotoUrl != null) {
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarRow(
    days: List<LocalDate>,
    selectedDay: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val formatterDia = remember { DateTimeFormatter.ofPattern("EEE", java.util.Locale("pt", "BR")) }
    val animatedAiGradient = rememberAnimatedAiGradient()

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(days) { index, date ->
            val isSelected = date == selectedDay
            val diaSemana = date.format(formatterDia).uppercase().take(3)
            val numeroDia = date.dayOfMonth.toString()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(55.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        brush = if (isSelected) animatedAiGradient else SolidColor(Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        onDateSelected(date)
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    }
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = diaSemana,
                    color = if (isSelected) CyanAI else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = numeroDia,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun HubButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Surface(
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Text(
                text = label,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        FloatingActionButton(
            onClick = onClick,
            containerColor = Color(0xFF121212),
            contentColor = color,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .size(56.dp)
                .neonGlow(color) 
                .border(1.dp, color, RoundedCornerShape(16.dp)),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}

fun Modifier.neonGlow(color: Color) = this.drawBehind {
    drawCircle(color = color.copy(alpha = 0.25f), radius = size.maxDimension * 0.85f, center = center)
}

@Composable
fun MoodChartCard(
    points: List<Float>,
    onHeaderClick: () -> Unit
) {
    val animatedAiGradient = rememberAnimatedAiGradient()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onHeaderClick() }
            .border(1.5.dp, animatedAiGradient, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Humor da Semana", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = CyanAI)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF0F0F0F), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (points.isEmpty()) {
                    Text("Sem dados para esta semana", color = Color.DarkGray, fontSize = 12.sp)
                } else {
                    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)) {
                        val width = size.width
                        val height = size.height
                        
                        if (points.size > 1) {
                            val spaceX = width / (points.size - 1)
                            val path = Path()
                            points.forEachIndexed { i, point ->
                                val x = i * spaceX
                                val y = height - (point * height / 5f)
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                drawCircle(CyanAI, radius = 3.dp.toPx(), center = Offset(x, y))
                            }
                            drawPath(path, CyanAI, style = Stroke(width = 2.dp.toPx()))
                        } else if (points.size == 1) {
                            val y = height - (points[0] * height / 5f)
                            drawCircle(CyanAI, radius = 4.dp.toPx(), center = Offset(width / 2, y))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateLottie() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.example.memoflow.R.raw.layout_vazio))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        LottieAnimation(composition, { progress }, modifier = Modifier.size(280.dp))
        Text(
            text = "Nenhuma memória no vácuo deste dia...",
            color = Color.Gray.copy(alpha = 0.6f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
