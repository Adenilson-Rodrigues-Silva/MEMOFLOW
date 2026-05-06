package com.arsdevstudio.memoflow.ui.screens.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsdevstudio.memoflow.ui.viewmodel.DateMark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronosCalendarSheet(
    onDismiss: () -> Unit,
    selectedDate: LocalDate,
    markedDates: Map<LocalDate, DateMark>,
    onDateSelected: (LocalDate) -> Unit,
    neonGreen: Color = Color(0xFF00FFC2)
) {
    val iceBlue = Color(0xFF80DEEA)
    val cyberpunkCyan = Color(0xFF00E5FF)
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var tempSelectedDate by remember { mutableStateOf(selectedDate) }
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "btn_effects")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0A0A),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "CHRONOS: NAVEGAÇÃO",
                color = neonGreen.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Row {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.White)
                    }
                    IconButton(
                        onClick = { if (currentMonth.isBefore(YearMonth.from(today))) currentMonth = currentMonth.plusMonths(1) },
                        enabled = currentMonth.isBefore(YearMonth.from(today))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = if (currentMonth.isBefore(YearMonth.from(today))) Color.White else Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val daysInMonth = currentMonth.lengthOfMonth()
            val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7
            val totalSlots = daysInMonth + firstDayOfMonth

            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(240.dp), userScrollEnabled = false) {
                items(totalSlots) { index ->
                    if (index >= firstDayOfMonth) {
                        val dayNum = index - firstDayOfMonth + 1
                        val date = currentMonth.atDay(dayNum)
                        val isSelected = date == tempSelectedDate
                        val isToday = date == today
                        val isFuture = date.isAfter(today)
                        val mark = markedDates[date]

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) neonGreen else Color.Transparent)
                                .then(
                                    if (isToday && !isSelected) {
                                        Modifier.border(1.5.dp, cyberpunkCyan.copy(alpha = glowIntensity), RoundedCornerShape(12.dp))
                                    } else Modifier
                                )
                                .clickable(enabled = !isFuture) { tempSelectedDate = date },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (!isSelected && !isFuture) {
                                    when (mark) {
                                        DateMark.Capsule -> Icon(Icons.Default.AcUnit, null, tint = iceBlue, modifier = Modifier.size(10.dp))
                                        DateMark.Locked -> Icon(Icons.Default.Lock, null, tint = neonGreen.copy(alpha = 0.6f), modifier = Modifier.size(10.dp))
                                        else -> {}
                                    }
                                }
                                Text(
                                    text = dayNum.toString(),
                                    color = if (isSelected) Color.Black else if (isToday) cyberpunkCyan else if (isFuture) Color.DarkGray else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected || isToday || mark != null) FontWeight.Bold else FontWeight.Normal
                                )
                                if (mark == DateMark.Normal && !isSelected && !isFuture) {
                                    Box(Modifier.size(4.dp).background(neonGreen, CircleShape).offset(y = 2.dp))
                                }
                            }
                        }
                    } else Box(Modifier.aspectRatio(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = neonGreen.copy(alpha = 0.1f * glowIntensity),
                            topLeft = Offset(-4.dp.toPx(), -4.dp.toPx()),
                            size = Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                        )
                    }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            scope.launch {
                                delay(200)
                                onDateSelected(tempSelectedDate)
                                onDismiss()
                            }
                        },
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF003D33), Color(0xFF001A14))
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    listOf(neonGreen.copy(alpha = 0.8f), Color.White.copy(alpha = 0.2f), neonGreen.copy(alpha = 0.8f))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "INICIAR SALTO TEMPORAL",
                                color = neonGreen,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.AutoMirrored.Filled.NavigateNext, null, tint = neonGreen, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}

