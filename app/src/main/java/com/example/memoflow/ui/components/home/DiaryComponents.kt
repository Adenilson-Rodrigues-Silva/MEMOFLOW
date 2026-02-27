package com.example.memoflow.ui.components.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.intl.Locale
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.memoflow.R



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

@Composable
fun PixelExplosion(atX: Float, atY: Float, onFinished: () -> Unit) {
    val particleCount = 30
    val animatables = remember { List(particleCount) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        animatables.forEachIndexed { index, anim ->
            anim.animateTo(1f, animationSpec = tween(600, delayMillis = index * 2))
        }
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        animatables.forEachIndexed { i, anim ->
            val progress = anim.value
            if (progress < 1f) {
                val angle = (i.toFloat() / particleCount) * 2f * Math.PI
                val velocity = 400f * (0.5f + (i % 5) / 5f)
                val offsetX = (Math.cos(angle) * velocity * progress).toFloat()
                val offsetY = (Math.sin(angle) * velocity * progress).toFloat() - (progress * 200f)

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

@Composable
fun HomeHeader(
    date: String,
    userPhotoUrl: String?,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Meu Diário",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                date,
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp
            )
        }

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
                    .background(if (isSelected) neonGreen else Color(0xFF1A1A1A))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f),
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
                    color = if (isSelected) Color.Black else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = numeroDia,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        }
    }
}


@Composable
fun EmptyState() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "🚀",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Nenhuma memória no vácuo deste dia...",
            color = Color(0xFF666666),
            fontSize = 14.sp
        )
    }
}


@Composable
fun MoodChartCard(neonGreen: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Humor da Semana",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF0F0F0F), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridAlpha = 0.05f
                    drawLine(Color.White.copy(gridAlpha), Offset(0f, size.height * 0.33f), Offset(size.width, size.height * 0.33f))
                    drawLine(Color.White.copy(gridAlpha), Offset(0f, size.height * 0.66f), Offset(size.width, size.height * 0.66f))
                }

                Text("Gráfico em progresso...", color = Color.DarkGray, fontSize = 12.sp)
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
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFF222222), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = time,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = content,
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
fun EmptyStateLottie() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.layout_vazio))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(280.dp)
        )
        Text(
            text = "Nenhuma memória no vácuo deste dia...",
            color = Color.Gray.copy(alpha = 0.6f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
