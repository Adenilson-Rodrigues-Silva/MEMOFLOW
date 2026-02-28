package com.example.memoflow.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory)
) {
    val statsData by viewModel.statsData.collectAsState()
    val neonGreen = Color(0xFF00FFC2)

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Estatísticas", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text("ESTATÍSTICAS AVANÇADAS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                MoodEvolutionCard(statsData.moodPoints, neonGreen)
            }

            item {
                HumorDistributionRow(statsData.moodDistribution)
            }

            item {
                WritingStatsCard(statsData.audioCount, statsData.textCount, neonGreen)
            }

            item {
                EntriesCountCard(statsData.entriesPerDay, neonGreen)
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun MoodEvolutionCard(points: List<Float>, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Evolução do Humor", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(20.dp))
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                if (points.size > 1) {
                    val spaceX = width / (points.size - 1)
                    val path = Path()
                    points.forEachIndexed { i, point ->
                        val x = i * spaceX
                        val y = height - (point * height / 5f)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(accentColor, radius = 4.dp.toPx(), center = Offset(x, y))
                    }
                    drawPath(path, accentColor, style = Stroke(width = 2.dp.toPx()))
                }
            }
        }
    }
}

@Composable
fun HumorDistributionRow(distribution: Map<String, Int>) {
    val total = distribution.values.sum().coerceAtLeast(1)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        distribution.forEach { (humor, count) ->
            val percent = (count * 100) / total
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(percent.toString() + "%", color = Color.White, fontWeight = FontWeight.Bold)
                Text(humor, color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun WritingStatsCard(audio: Int, text: Int, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Áudio vs Texto", color = Color.White, fontWeight = FontWeight.Bold)
                Text("$audio Áudios | $text Textos", color = Color.Gray, fontSize = 14.sp)
            }
            Box(modifier = Modifier.size(50.dp).border(4.dp, accentColor, CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EntriesCountCard(entries: List<Int>, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Quantidade de Entradas", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceEvenly) {
                entries.forEach { count ->
                    val barHeight = (count * 30).coerceAtMost(100).dp
                    Box(modifier = Modifier.width(12.dp).height(barHeight).background(accentColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                }
            }
        }
    }
}
