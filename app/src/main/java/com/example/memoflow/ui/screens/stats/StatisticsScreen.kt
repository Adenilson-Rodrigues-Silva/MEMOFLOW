package com.example.memoflow.ui.screens.stats

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.*
import com.example.memoflow.R
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
                            viewModel.setPeriodInt(0)
                        }
                        PeriodTab(Modifier.weight(1f), "Mensal", selectedTab == 1, dataGradient) { 
                            selectedTab = 1
                            viewModel.setPeriodInt(1)
                        }
                    }
                }
            }

            // --- AI INSIGHTS CARD ---
            item {
                val isPremium by viewModel.isPremium.collectAsState()
                SectionHeader("Inteligência Artificial", isAi = true)
                AiInsightCard(
                    insight = statsData.aiInsight,
                    isPremium = isPremium,
                    onGenerateWeekly = { viewModel.generateAiInsights("weekly") },
                    onGenerateMonthly = { viewModel.generateAiInsights("monthly") },
                    onTodayGenerate = { viewModel.generateAiInsights("today") },
                    borderBrush = dataGradient
                )
            }

            if (statsData.aiInsight.isLoading) {
                item {
                    MemoFlowAiLoadingDialog()
                }
            }

            // --- NOVO CARD: LUGAR MAIS FELIZ ---
            statsData.happiestCity?.let { cityStat ->
                item {
                    SectionHeader("Geografia da Felicidade")
                    HappiestCityCard(cityStat, dataGradient)
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
fun HappiestCityCard(cityStat: CityHumorStat, borderBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.5.dp, borderBrush, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6A00FF).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF6A00FF), modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Seu Lugar Mais Feliz", color = Color.Gray, fontSize = 12.sp)
                Text(
                    text = cityStat.cityName, 
                    color = Color.White, 
                    fontSize = 22.sp, 
                    fontWeight = FontWeight.ExtraBold 
                )
                Text(
                    text = "Você costuma estar mais vibrante aqui!",
                    color = Color(0xFF00FFC2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
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
fun SectionHeader(title: String, isAi: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title.uppercase(),
            color = if (isAi) DataGreen else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        if (isAi) {
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.AutoAwesome, null, tint = DataGreen, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun AiInsightCard(
    insight: AiInsightData,
    isPremium: Boolean,
    onGenerateWeekly: () -> Unit,
    onGenerateMonthly: () -> Unit,
    onTodayGenerate: () -> Unit,
    borderBrush: Brush
) {
    val aiGradient = remember {
        Brush.linearGradient(
            colors = listOf(Color(0xFF00FFC2), Color(0xFF00E5FF), Color(0xFF7C4DFF))
        )
    }

    val timerColor = Color(0xFF7C4DFF)

    val infiniteTransition = rememberInfiniteTransition(label = "ai_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF00FFC2).copy(alpha = glowAlpha)
            )
            .border(2.dp, aiGradient, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF00FFC2),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "MemoFlow AI Insight",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                
                if (insight.summary.isNotEmpty()) {
                    Surface(
                        color = Color(0xFF00FFC2).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF00FFC2).copy(alpha = 0.3f))
                    ) {
                        Text(
                            when(insight.currentScope) {
                                "today" -> "HOJE"
                                "weekly" -> "SEMANAL"
                                "monthly" -> "MENSAL"
                                else -> "INSIGHT"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(0xFF00FFC2),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (!isPremium) {
                    Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            if (!isPremium) {
                Text(
                    "Assine o Premium para desbloquear resumos semanais e análises de humor geradas por Inteligência Artificial.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { /* Navegar para Loja */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("VER PLANOS PREMIUM", color = Color.Black, fontWeight = FontWeight.Black)
                }
            } else {
                if (insight.summary.isEmpty() && !insight.isLoading) {
                    Text(
                        "Sua jornada merece ser compreendida. A IA analisará suas notas para encontrar padrões, motivação e insights sobre o seu bem-estar.",
                        color = Color.LightGray,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            Triple("HOJE", "today", onTodayGenerate),
                            Triple("SEMANA", "weekly", onGenerateWeekly),
                            Triple("MÊS", "monthly", onGenerateMonthly)
                        ).forEach { (label, scope, action) ->
                            val nextTime = insight.nextAvailableTime[scope] ?: 0L
                            val isCooldown = System.currentTimeMillis() < nextTime

                            Button(
                                onClick = action,
                                enabled = !isCooldown,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isCooldown) Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.1f), Color.Gray.copy(alpha = 0.1f)))
                                            else aiGradient, 
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCooldown) {
                                        val remaining = (nextTime - System.currentTimeMillis()) / 1000
                                        Text("${remaining / 60}m ${remaining % 60}s", color = timerColor, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("REVELAR INSIGHT $label", color = Color.Black, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }

                    insight.error?.let {
                        Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp), textAlign = TextAlign.Center)
                    }
                } else if (insight.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00FFC2), strokeWidth = 3.dp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            insight.summary,
                            color = Color.White,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    
                    if (insight.sentimentScores.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "MAPA DA ALMA (EVOLUÇÃO IA)",
                            color = Color(0xFF00FFC2),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AiSentimentEvolutionChart(insight.sentimentScores)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("HOJE", "today", onTodayGenerate),
                            Triple("SEMANA", "weekly", onGenerateWeekly),
                            Triple("MÊS", "monthly", onGenerateMonthly)
                        ).forEach { (label, scope, action) ->
                            val nextTime = insight.nextAvailableTime[scope] ?: 0L
                            val isCooldown = System.currentTimeMillis() < nextTime

                            Button(
                                onClick = action,
                                enabled = !isCooldown,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isCooldown) Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.1f), Color.Gray.copy(alpha = 0.1f)))
                                            else if (insight.currentScope == scope) Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f)))
                                            else aiGradient, 
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCooldown) {
                                        val remaining = (nextTime - System.currentTimeMillis()) / 1000
                                        Text("${remaining / 60}m ${remaining % 60}s", color = timerColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text(label, color = if(insight.currentScope == scope) Color.White else Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoFlowAiLoadingDialog() {
    val loadingMessages = remember {
        listOf(
            "MemoFlow AI está conectando os pontos...",
            "Acelerando com a tecnologia Groq ⚡",
            "Refletindo sobre suas memórias...",
            "Llama 3.1 processando em velocidade máxima!",
            "Sua jornada está sendo resumida agora..."
        )
    }
    val currentMessage = remember { loadingMessages.random() }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Color(0xFF121212), RoundedCornerShape(32.dp))
                .border(1.5.dp, rememberDataAiGradient(), RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_groq))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(180.dp),
                    renderMode = RenderMode.SOFTWARE
                )
                
                Spacer(Modifier.height(16.dp))
                Text(
                    currentMessage,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Quase pronto! A IA é veloz.",
                    color = DataGreen,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AiSentimentEvolutionChart(scores: List<Float>) {
    val moods = listOf("😊", "😄", "🙂", "😐", "😕", "😔", "😢")
    val days = listOf("S", "T", "Q", "Q", "S", "S", "D")

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            // Eixo Vertical (Humores) - 7 níveis
            Column(
                modifier = Modifier.fillMaxHeight().width(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                moods.forEach { Text(it, fontSize = 14.sp) }
            }

            Spacer(Modifier.width(12.dp))

            // Área do Gráfico (A Grade Invisível 7x7)
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val colWidth = width / 7f
                    val rowHeight = height / 7f

                    // Desenha linhas horizontais sutis de guia
                    for (i in 0..6) {
                        val y = i * rowHeight + (rowHeight / 2f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Linha Neutra (😐) - Destaque no meio (index 3)
                    val neutralY = 3 * rowHeight + (rowHeight / 2f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = Offset(0f, neutralY),
                        end = Offset(width, neutralY),
                        strokeWidth = 1.dp.toPx()
                    )

                    if (scores.isNotEmpty()) {
                        val path = Path()
                        val points = scores.take(7) // Garante que pegamos no máximo 7 dias
                        
                        points.forEachIndexed { i, score ->
                            // Posiciona o X no centro de cada uma das 7 colunas
                            val x = (i * colWidth) + (colWidth / 2f)
                            
                            // Mapeia o score (0.0 a 1.0) para o Y
                            // 1.0 (Muito Feliz) -> Topo (0)
                            // 0.0 (Muito Triste) -> Fundo (height)
                            val y = height - (score * height)
                            
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            
                            // Desenha o ponto com um pequeno brilho
                            drawCircle(DataGreen, radius = 4.dp.toPx(), center = Offset(x, y))
                            drawCircle(DataGreen.copy(alpha = 0.3f), radius = 8.dp.toPx(), center = Offset(x, y))
                        }
                        
                        // Desenha a linha conectora suave
                        drawPath(
                            path = path,
                            color = DataGreen,
                            style = Stroke(
                                width = 3.dp.toPx(), 
                                cap = StrokeCap.Round, 
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Eixo Horizontal (Dias da Semana)
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 36.dp), 
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEach { day ->
                Text(
                    text = day,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MoodChartProfessional(points: List<Float>, labels: List<String>, color: Color, borderBrush: Brush) {
    val verticalLabels = listOf("Incrível", "", "", "Neutro", "", "", "Muito Triste")
    
    Card(
        modifier = Modifier.fillMaxWidth().height(250.dp).border(1.5.dp, borderBrush, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Eixo Vertical (Texto)
            Column(
                modifier = Modifier.fillMaxHeight().width(60.dp).padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                verticalLabels.forEach { label ->
                    if (label.isNotEmpty()) {
                        Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val rowHeight = height / 6f // 7 pontos criam 6 espaços
                        
                        // Desenha as 7 linhas horizontais da grade
                        for (i in 0..6) {
                            val y = i * rowHeight
                            drawLine(
                                color = if (i == 3) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = if (i == 3) 1.5.dp.toPx() else 1.dp.toPx()
                            )
                        }

                        if (points.isNotEmpty()) {
                            val spaceX = if (points.size > 1) width / (points.size - 1) else width
                            val path = Path()
                            
                            points.forEachIndexed { i, pt ->
                                val x = if (points.size > 1) i * spaceX else width / 2f
                                
                                // Mapeia pt (0.0 a 1.0) para y. 
                                // 1.0 (Incrível) -> y = 0
                                // 0.0 (Triste) -> y = height
                                val y = height - (pt * height)
                                
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                
                                drawCircle(DataGreen.copy(alpha = 0.3f), radius = 6.dp.toPx(), center = Offset(x, y))
                                drawCircle(DataGreen, radius = 3.dp.toPx(), center = Offset(x, y))
                            }
                            
                            if (points.size > 1) {
                                drawPath(
                                    path = path, 
                                    color = DataGreen, 
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Eixo Horizontal (Labels/Dias)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val step = if (labels.size > 7) (labels.size / 7).coerceAtLeast(1) else 1
                    labels.forEachIndexed { index, label ->
                        if (index % step == 0 || index == labels.lastIndex) {
                            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
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
