package com.example.memoflow.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.intl.Locale
//import com.google.android.libraries.places.api.model.LocalDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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
                .neonGlow(color) // O efeito de brilho que criamos
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
fun HomeHeader(date: String) {

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

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF00FFC2), CircleShape)
        )
    }
}

@Composable
fun CalendarRow(
    days: List<LocalDate>,
    selectedDay: LocalDate,
    neonGreen: Color,
    onDaySelected: (LocalDate) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    // Formatter para o nome do dia (ex: QUI)
    val formatterDia = remember { DateTimeFormatter.ofPattern("EEE", java.util.Locale("pt", "BR")) }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), // Reduzi um pouco o espaço
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(days) { index, date ->
            val isSelected = date == selectedDay
            val diaSemana = date.format(formatterDia).uppercase().take(3)
            val numeroDia = date.dayOfMonth.toString()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(55.dp) // DIMINUI de 65dp para 55dp para ficar menos largo
                    .clip(RoundedCornerShape(16.dp)) // Cantos menos arredondados (mais moderno)
                    .background(if (isSelected) neonGreen else Color(0xFF1A1A1A))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        onDaySelected(date)
                        coroutineScope.launch {
                            // Centraliza o item ao clicar
                            listState.animateScrollToItem(index)
                        }
                    }
                    .padding(vertical = 12.dp) // DIMINUI de 16dp para 12dp para ficar mais baixo
            ) {
                Text(
                    text = diaSemana,
                    color = if (isSelected) Color.Black else Color.Gray,
                    fontSize = 10.sp, // Diminui levemente a fonte
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp)) // Espaço menor entre textos
                Text(
                    text = numeroDia,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp // Diminui de 20sp para 18sp
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

