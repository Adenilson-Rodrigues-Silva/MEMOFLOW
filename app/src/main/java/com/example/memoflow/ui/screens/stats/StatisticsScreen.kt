package com.example.memoflow.ui.screens.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memoflow.ui.components.home.rememberAnimatedAiGradient
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

// Cores Temáticas para Estatísticas (Cyber Data)
val DataGreen = Color(0xFF00FFC2)
val DataCyan = Color(0xFF00E5FF)

@Composable
fun rememberDataAiGradient(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "data_gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "offset"
    )
    
    return Brush.linearGradient(
        colors = listOf(DataGreen, DataCyan, DataGreen),
        start = Offset(offset, offset),
        end = Offset(offset + 1000f, offset + 1000f),
        tileMode = TileMode.Repeated
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory)
) {
    val statsData by viewModel.statsData.collectAsState()
    val neonGreen = Color(0xFF00FFC2)
    val iceBlue = Color(0xFF80DEEA)
    val neonYellow = Color(0xFFFFFF00)
    var selectedTab by remember { mutableIntStateOf(0) }
    
    var showSpecialDaysDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("") } 

    val dataGradient = rememberDataAiGradient()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Estatísticas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF1A1A1A))
                        .border(1.dp, dataGradient, RoundedCornerShape(26.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        PeriodTab(Modifier.weight(1f), "Semanal", selectedTab == 0, dataGradient) { 
                            selectedTab = 0
                            viewModel.setPeriod(0)
                        }
                        PeriodTab(Modifier.weight(1f), "Mensal", selectedTab == 1, dataGradient) { 
                            selectedTab = 1
                            viewModel.setPeriod(1)
                        }
                    }
                }
            }

            item {
                SectionHeader("Luz no Pote")
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.5.dp, dataGradient, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(neonYellow.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = neonYellow)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "${statsData.gratitudeCount} novas gratidões",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "Total no pote: ${statsData.totalGratitudesInPote}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                if (selectedTab == 1) {
                    SectionHeader("Termômetro de Humores")
                    MonthlyMoodThermometer(statsData.topMoods, statsData.monthName, dataGradient)
                } else {
                    SectionHeader("Resumo da Semana")
                    WeeklyMoodInsight(statsData.topMoods, dataGradient)
                }
            }

            item {
                Column {
                    SectionHeader(if (selectedTab == 0) "Tendência Semanal" else "Tendência Mensal")
                    MoodChartProfessional(statsData.moodPoints, statsData.dayLabels, neonGreen, dataGradient)
                }
            }

            item {
                SectionHeader("Segredos e Futuro")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InsightCardClickable(
                        Modifier.weight(1f), 
                        "Trancadas", 
                        statsData.lockedCount.toString(), 
                        "notas", 
                        Icons.Default.Lock, 
                        neonGreen,
                        dataGradient,
                        onClick = { filterType = "Lock"; showSpecialDaysDialog = true }
                    )
                    InsightCardClickable(
                        Modifier.weight(1f), 
                        "Congeladas", 
                        statsData.capsuleCount.toString(), 
                        "notas", 
                        Icons.Default.AcUnit, 
                        iceBlue,
                        dataGradient,
                        onClick = { filterType = "Capsule"; showSpecialDaysDialog = true }
                    )
                }
            }

            item {
                SectionHeader("Volume de Memórias")
                EntriesBarChart(statsData.entriesPerDay, statsData.dayLabels, neonGreen, dataGradient)
            }

            item {
                MediaSummaryCard(statsData.audioCount, statsData.imageCount, neonGreen, dataGradient)
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showSpecialDaysDialog) {
            val days = if (filterType == "Lock") statsData.lockedDays else statsData.capsuleDays
            AlertDialog(
                onDismissRequest = { showSpecialDaysDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { 
                    Text(
                        if (filterType == "Lock") "Dias com Cadeado 🔒" else "Dias com Cápsula ❄️", 
                        color = Color.White 
                    ) 
                },
                text = {
                    Column {
                        if (days.isEmpty()) {
                            Text("Nenhum registro encontrado neste período.", color = Color.Gray)
                        } else {
                            days.forEach { date ->
                                Text(
                                    text = "• ${date.dayOfMonth}/${date.monthValue}/${date.year}",
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSpecialDaysDialog = false }) {
                        Text("FECHAR", color = neonGreen)
                    }
                }
            )
        }
    }
}

@Composable
fun MonthlyMoodThermometer(moods: List<MoodStat>, monthName: String, borderBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.5.dp, borderBrush, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Resumo de $monthName",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            val dominantMood = moods.firstOrNull()
            if (dominantMood != null) {
                Text(
                    text = "Este mês você esteve predominantemente ${dominantMood.label.lowercase()}.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        moods.forEach { mood ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(mood.percentage.toFloat().coerceAtLeast(0.1f))
                                    .background(mood.color)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Sem registros suficientes.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            moods.forEach { mood ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(mood.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(mood.label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(
                        "${mood.count} ${if (mood.count == 1) "nota" else "notas"}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "${mood.percentage}%",
                        color = mood.color,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyMoodInsight(moods: List<MoodStat>, borderBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.5.dp, borderBrush, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dominantMood = moods.firstOrNull()
            if (dominantMood != null) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(dominantMood.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(dominantMood.emoji, fontSize = 32.sp)
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text("Humor Dominante", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        dominantMood.label, 
                        color = Color.White, 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.ExtraBold 
                    )
                    Text(
                        "Presente em ${dominantMood.percentage}% da sua semana.",
                        color = dominantMood.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text("Inicie seus registros para ver o resumo da semana!", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun InsightCardClickable(modifier: Modifier, title: String, value: String, unit: String, icon: ImageVector, color: Color, borderBrush: Brush, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() }.border(1.5.dp, borderBrush, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.Gray, fontSize = 11.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(unit, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PeriodTab(modifier: Modifier, text: String, isSelected: Boolean, activeGradient: Brush, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF333333) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, 
            color = if (isSelected) DataCyan else Color.Gray, 
            fontSize = 14.sp, 
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = Color.Gray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun MoodChartProfessional(points: List<Float>, labels: List<String>, color: Color, borderBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp).border(1.5.dp, borderBrush, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (points.size < 2) return@Canvas
                val width = size.width
                val height = size.height
                val spaceX = width / (points.size - 1)
                
                val path = Path()
                points.forEachIndexed { i, pt ->
                    val x = i * spaceX
                    val y = height - (pt * height / 5f)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(DataGreen, radius = 4.dp.toPx(), center = Offset(x, y))
                }
                drawPath(path, DataGreen, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val step = if (labels.size > 7) 5 else 1
                labels.forEachIndexed { index, label ->
                    if (index % step == 0 || index == labels.lastIndex) {
                        Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EntriesBarChart(entries: List<Int>, labels: List<String>, color: Color, borderBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp).border(1.5.dp, borderBrush, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Column(modifier = Modifier.padding(top = 20.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                entries.forEach { count ->
                    val barHeight = (count * 25).coerceAtLeast(6).coerceAtMost(100).dp
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(Brush.verticalGradient(listOf(DataGreen, DataCyan.copy(alpha = 0.3f))))
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                val step = if (labels.size > 7) 5 else 1
                labels.forEachIndexed { index, label ->
                    if (index % step == 0 || index == labels.lastIndex) {
                        Text(
                            text = label, 
                            modifier = Modifier.weight(1f), 
                            textAlign = TextAlign.Center, 
                            color = Color.Gray, 
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    } else if (labels.size <= 7) {
                         Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun MediaSummaryCard(audios: Int, images: Int, color: Color, borderBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.5.dp, borderBrush, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            MediaItemStats(audios, "Áudios", Icons.Default.Mic, DataCyan)
            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.1f)))
            MediaItemStats(images, "Fotos", Icons.Default.Image, DataCyan)
        }
    }
}

@Composable
fun MediaItemStats(count: Int, label: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(count.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Color.Gray, fontSize = 11.sp)
        }
    }
}
