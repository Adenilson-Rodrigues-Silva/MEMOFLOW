package com.example.memoflow.ui.screens.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory)
) {
    val statsData by viewModel.statsData.collectAsState()
    val neonGreen = Color(0xFF00FFC2)
    val iceBlue = Color(0xFF80DEEA)
    var selectedTab by remember { mutableIntStateOf(0) }
    
    var showSpecialDaysDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("") } // "Lock" ou "Capsule"

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
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1A1A1A))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        PeriodTab(Modifier.weight(1f), "Semanal", selectedTab == 0) { 
                            selectedTab = 0
                            viewModel.setPeriod(0)
                        }
                        PeriodTab(Modifier.weight(1f), "Mensal", selectedTab == 1) { 
                            selectedTab = 1
                            viewModel.setPeriod(1)
                        }
                    }
                }
            }

            // --- BLOCO DE HUMOR (AGRUPADO) ---
            item {
                Column {
                    SectionHeader("Análise de Humor")
                    MoodChartProfessional(statsData.moodPoints, statsData.dayLabels, neonGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    HumorDistributionPremium(statsData.topMoods)
                }
            }

            item {
                SectionHeader("Evolução Pessoal")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InsightCard(
                        Modifier.weight(1f),
                        "Concluídas",
                        statsData.goalsCompleted.toString(),
                        "metas",
                        Icons.Default.EmojiEvents,
                        Color(0xFFFFD700)
                    )
                    InsightCard(
                        Modifier.weight(1f),
                        "Ativas",
                        statsData.goalsActive.toString(),
                        "foco",
                        Icons.AutoMirrored.Filled.TrendingUp,
                        neonGreen
                    )
                }
            }

            if (statsData.categoryDistribution.isNotEmpty()) {
                item {
                    SectionHeader("Áreas de Foco")
                    GoalCategoryDistribution(statsData.categoryDistribution)
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
                        onClick = { filterType = "Lock"; showSpecialDaysDialog = true }
                    )
                    InsightCardClickable(
                        Modifier.weight(1f), 
                        "Congeladas", 
                        statsData.capsuleCount.toString(), 
                        "notas", 
                        Icons.Default.AcUnit, 
                        iceBlue,
                        onClick = { filterType = "Capsule"; showSpecialDaysDialog = true }
                    )
                }
            }

            item {
                SectionHeader("Volume de Memórias")
                EntriesBarChart(statsData.entriesPerDay, statsData.dayLabels, neonGreen)
            }

            item {
                MediaSummaryCard(statsData.audioCount, statsData.imageCount, neonGreen)
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
fun InsightCard(modifier: Modifier, title: String, value: String, unit: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
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
fun GoalCategoryDistribution(categories: List<GoalStat>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.forEach { stat ->
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stat.category, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${stat.count} metas", color = Color.Gray, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.Gray.copy(alpha = 0.1f), CircleShape)) {
                        Box(modifier = Modifier
                            .fillMaxWidth(if (categories.maxOf { it.count } > 0) stat.count.toFloat() / categories.maxOf { it.count } else 0f)
                            .fillMaxHeight()
                            .background(stat.color, CircleShape))
                    }
                }
            }
        }
    }
}

@Composable
fun InsightCardClickable(modifier: Modifier, title: String, value: String, unit: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
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
fun PeriodTab(modifier: Modifier, text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF333333) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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
fun MoodChartProfessional(points: List<Float>, labels: List<String>, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
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
                    drawCircle(color, radius = 4.dp.toPx(), center = Offset(x, y))
                }
                drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
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
fun HumorDistributionPremium(moods: List<MoodStat>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            moods.forEach { mood ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(mood.emoji, fontSize = 24.sp)
                    Text("${mood.percentage}%", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(mood.label, color = mood.color, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun EntriesBarChart(entries: List<Int>, labels: List<String>, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
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
                            .background(Brush.verticalGradient(listOf(color, color.copy(alpha = 0.3f))))
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
fun MediaSummaryCard(audios: Int, images: Int, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            MediaItemStats(audios, "Áudios", Icons.Default.Mic, color)
            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.1f)))
            MediaItemStats(images, "Fotos", Icons.Default.Image, color)
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
