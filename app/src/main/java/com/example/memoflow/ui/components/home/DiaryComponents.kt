package com.example.memoflow.ui.components.home

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.SolidColor
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.memoflow.R
import java.util.Date

// Cores do Efeito IA
val CyanAI = Color(0xFF00E5FF)
val PurpleAI = Color(0xFFD500F9)

// Gradiente Brilhante Global (Prateado/Cinza/Branco)
val GlobalSilverGradient = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.8f),
        Color(0xFFC0C0C0), 
        Color(0xFFE0E0E0), 
        Color(0xFF8E8E8E), 
        Color.White.copy(alpha = 0.8f)
    )
)

@Composable
fun rememberAnimatedAiGradient(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_gradient")
    
    // Animamos de 0 a 2000 para cobrir uma área maior e permitir um loop suave
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "offset"
    )
    
    // Usamos TileMode.Repeated para que o gradiente se repita infinitamente
    // e as cores Cyan -> Purple -> Cyan garantem que o início e fim do ciclo sejam iguais
    return Brush.linearGradient(
        colors = listOf(CyanAI, PurpleAI, CyanAI),
        start = Offset(offset, offset),
        end = Offset(offset + 1000f, offset + 1000f),
        tileMode = TileMode.Repeated
    )
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
fun HomeHeader(
    date: String,
    userPhotoUrl: String?,
    onProfileClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
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
    neonGreen: Color,
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
                    .background(if (isSelected) Color(0xFF1A1A1A) else Color(0xFF1A1A1A))
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
    val cyan = Color(0xFF00E5FF)
    val iceBlue = Color(0xFFB3E5FC)
    val deepBlue = Color(0xFF01579B)
    val forestGreen = Color(0xFF1B5E20)
    val silverWhite = Color(0xFFE0E0E0)
    val darkSilver = Color(0xFF424242)
    
    val infiniteTransition = rememberInfiniteTransition(label = "global_effects")
    
    val animationValue by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "border_pulse"
    )

    val borderBrush = when {
        isTimeCapsule -> Brush.linearGradient(
            listOf(cyan, iceBlue, deepBlue, cyan),
            start = Offset.Zero,
            end = Offset.Infinite
        )
        isLocked -> Brush.linearGradient(
            listOf(neonGreen, forestGreen, neonGreen),
            start = Offset.Zero,
            end = Offset.Infinite
        )
        else -> Brush.linearGradient(
            listOf(darkSilver, silverWhite.copy(alpha = animationValue), darkSilver),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(95.dp)
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .border(
                width = 1.5.dp, 
                brush = borderBrush,
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                when {
                    isTimeCapsule -> Color.Black.copy(alpha = 0.9f)
                    isLocked -> Color.Black.copy(alpha = 0.95f)
                    else -> Color(0xFF161616).copy(alpha = 0.85f)
                }
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        when {
                            isTimeCapsule -> cyan.copy(alpha = 0.15f)
                            isLocked -> neonGreen.copy(alpha = 0.15f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }, 
                        CircleShape
                    )
                    .border(
                        1.5.dp, 
                        when {
                            isTimeCapsule -> cyan.copy(alpha = 0.6f)
                            isLocked -> neonGreen.copy(alpha = 0.6f)
                            else -> silverWhite.copy(alpha = 0.3f)
                        }, 
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isTimeCapsule -> Icon(Icons.Default.AcUnit, null, tint = cyan, modifier = Modifier.size(26.dp))
                    isLocked -> Icon(Icons.Default.Lock, null, tint = neonGreen, modifier = Modifier.size(26.dp))
                    else -> Text(text = emoji, fontSize = 26.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = time, color = Color.Gray, fontSize = 11.sp)
                Text(
                    text = when {
                        isTimeCapsule -> "Cápsula do Tempo"
                        isLocked -> "Memória Trancada"
                        else -> title
                    },
                    color = when {
                        isTimeCapsule -> cyan
                        isLocked -> neonGreen
                        else -> Color.White
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = when {
                        isTimeCapsule -> "Disponível em ${unlockDate?.let { java.text.SimpleDateFormat("dd/MM/yyyy").format(Date(it)) }}"
                        isLocked -> "Segredo protegido por PIN"
                        else -> content
                    },
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MoodChartCard(
    neonGreen: Color,
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
                Icon(Icons.Default.KeyboardArrowRight, null, tint = CyanAI)
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
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.layout_vazio))
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
