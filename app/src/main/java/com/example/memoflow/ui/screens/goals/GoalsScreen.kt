package com.example.memoflow.ui.screens.goals

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.memoflow.data.local.entity.GoalEntity
import com.example.memoflow.navigation.Screen
import com.example.memoflow.ui.viewmodel.GoalFilter
import com.example.memoflow.ui.viewmodel.GoalsViewModel
import com.example.memoflow.ui.viewmodel.HomeViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onBack: () -> Unit,
    navController: NavController,
    viewModel: GoalsViewModel = viewModel(factory = GoalsViewModel.Factory),
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.goalsState.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val notes by homeViewModel.notes.collectAsState()

    val context = LocalContext.current
    val neonGreen = Color(0xFF00FFC2)
    val iceBlue = Color(0xFF80DEEA)
    
    var showAddGoalSheet by remember { mutableStateOf(false) }
    var goalToDelete by remember { mutableStateOf<GoalEntity?>(null) }
    var goalCompletedToCelebrate by remember { mutableStateOf<GoalEntity?>(null) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("METAS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalSheet = true },
                containerColor = neonGreen,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Adicionar Meta", tint = Color.Black)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                GoalsInspirationalHeader(
                    progressRate = stats.progressRate,
                    completedCount = stats.completedThisMonth,
                    streak = stats.activeStreak,
                    neonGreen = neonGreen
                )
            }

            item {
                AchievementsSection(
                    neonGreen = neonGreen,
                    completedCount = stats.completedThisMonth,
                    streak = stats.activeStreak
                )
            }

            item {
                FilterRow(
                    selectedFilter = currentFilter,
                    onFilterSelected = { viewModel.setFilter(it) },
                    neonGreen = neonGreen
                )
            }

            if (uiState.goals.isEmpty()) {
                item {
                    EmptyGoalsState(neonGreen)
                }
            } else {
                items(uiState.goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        neonGreen = neonGreen,
                        iceBlue = iceBlue,
                        onUpdateProgress = { 
                            viewModel.updateNumericProgress(goal, it)
                            if (it >= goal.targetValue && !goal.isCompleted) goalCompletedToCelebrate = goal
                        },
                        onToggleStep = { 
                            viewModel.toggleStep(goal, it)
                            // A lógica de conclusão pode ser verificada aqui também se necessário
                        },
                        onIncrementStreak = { 
                            viewModel.incrementStreak(goal)
                            if (goal.streak + 1 == 21) goalCompletedToCelebrate = goal
                        },
                        onDelete = { goalToDelete = goal }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddGoalSheet) {
        AddGoalBottomSheet(
            onDismiss = { showAddGoalSheet = false },
            onGoalCreated = { 
                viewModel.addGoal(it)
                showAddGoalSheet = false
            },
            neonGreen = neonGreen
        )
    }

    if (goalToDelete != null) {
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Excluir Meta?", color = Color.White) },
            text = { Text("Deseja realmente apagar esta meta? Todo o progresso será perdido.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    goalToDelete?.let { viewModel.deleteGoal(it) }
                    goalToDelete = null
                }) { Text("EXCLUIR", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) { Text("CANCELAR", color = Color.White) }
            }
        )
    }

    if (goalCompletedToCelebrate != null) {
        AlertDialog(
            onDismissRequest = { goalCompletedToCelebrate = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Conquista Épica! 🎉", color = Color.White) },
            text = { 
                Column {
                    Text("Você completou a meta: ${goalCompletedToCelebrate?.title}", color = neonGreen, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Que tal registrar como você se sente agora no seu diário?", color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (notes.size < 3) {
                            goalCompletedToCelebrate = null
                            navController.navigate(Screen.WriteNote.createRoute())
                        } else {
                            Toast.makeText(context, "Limite de 3 notas por dia atingido!", Toast.LENGTH_LONG).show()
                            goalCompletedToCelebrate = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen)
                ) {
                    Text("ESCREVER NO DIÁRIO", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { goalCompletedToCelebrate = null }) {
                    Text("AGORA NÃO", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun GoalsInspirationalHeader(
    progressRate: Float,
    completedCount: Int,
    streak: Int,
    neonGreen: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quem você está tentando se tornar?",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 32.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = neonGreen.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, neonGreen.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Você está ${progressRate.toInt()}% mais consistente que no mês passado.",
                color = neonGreen,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                label = "Sequência",
                value = "$streak 🔥",
                modifier = Modifier.weight(1f),
                color = neonGreen
            )
            StatBox(
                label = "Concluídas",
                value = completedCount.toString(),
                modifier = Modifier.weight(1f),
                color = Color(0xFF80DEEA)
            )
            StatBox(
                label = "Progresso",
                value = "${progressRate.toInt()}%",
                modifier = Modifier.weight(1f),
                color = Color(0xFFFFC107)
            )
        }
    }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier, color: Color) {
    Surface(
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun AchievementsSection(neonGreen: Color, completedCount: Int, streak: Int) {
    Column {
        Text("Conquistas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            item { AchievementBadge("🔥 7 Dias", streak >= 7, neonGreen) }
            item { AchievementBadge("📘 10 Metas", completedCount >= 10, neonGreen) }
            item { AchievementBadge("💪 Difícil", completedCount >= 1, Color(0xFFFF5252)) }
            item { AchievementBadge("🧠 Resiliente", streak >= 1, Color(0xFF80DEEA)) }
        }
    }
}

@Composable
fun AchievementBadge(label: String, isUnlocked: Boolean, color: Color) {
    Surface(
        color = if (isUnlocked) color.copy(alpha = 0.15f) else Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp),
        border = if (isUnlocked) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (isUnlocked) color else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isUnlocked) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun FilterRow(
    selectedFilter: GoalFilter,
    onFilterSelected: (GoalFilter) -> Unit,
    neonGreen: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GoalFilterChip("Ativas", selectedFilter == GoalFilter.ACTIVE, neonGreen) { onFilterSelected(GoalFilter.ACTIVE) }
        GoalFilterChip("Feitas", selectedFilter == GoalFilter.COMPLETED, Color(0xFF4CAF50)) { onFilterSelected(GoalFilter.COMPLETED) }
        GoalFilterChip("Atrasadas", selectedFilter == GoalFilter.OVERDUE, Color(0xFFF44336)) { onFilterSelected(GoalFilter.OVERDUE) }
        GoalFilterChip("Foco", selectedFilter == GoalFilter.IMPORTANT, Color(0xFFFFC107)) { onFilterSelected(GoalFilter.IMPORTANT) }
    }
}

@Composable
fun GoalFilterChip(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSelected) color else Color.Gray.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            color = if (isSelected) color else Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    neonGreen: Color,
    iceBlue: Color,
    onUpdateProgress: (Float) -> Unit,
    onToggleStep: (String) -> Unit,
    onIncrementStreak: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val progress = if (goal.targetValue > 0) goal.currentValue / goal.targetValue else 0f
    
    val categoryColor = when(goal.category) {
        "Saúde" -> Color(0xFF81C784)
        "Estudo" -> Color(0xFF64B5F6)
        "Financeiro" -> Color(0xFFFFF176)
        "Relacionamento" -> Color(0xFFF06292)
        else -> iceBlue
    }

    val rhythm = if (progress >= 0.8f) "No ritmo 🟢" else if (progress >= 0.4f) "Atenção 🟡" else "Atrasada 🔴"

    Surface(
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(categoryColor))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(goal.category.uppercase(), color = categoryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(goal.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                if (goal.priority == "Alta") {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(1000, easing = FastOutSlowInEasing),
                label = "progress"
            )

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = when(goal.type) {
                            "CONSISTENCY" -> "${goal.streak} dias de sequência"
                            "NUMERIC" -> "${goal.currentValue.toInt()}/${goal.targetValue.toInt()} ${goal.unit}"
                            "STEPS" -> "${goal.currentValue.toInt()}/${goal.targetValue.toInt()} etapas"
                            else -> ""
                        },
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text("${(progress * 100).toInt()}%", color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = neonGreen,
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(rhythm, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                
                if (goal.type == "STEPS") {
                    goal.steps.forEach { step ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = step.isCompleted,
                                onCheckedChange = { onToggleStep(step.id) },
                                colors = CheckboxDefaults.colors(checkedColor = neonGreen)
                            )
                            Text(step.title, color = if (step.isCompleted) Color.Gray else Color.White, fontSize = 14.sp)
                        }
                    }
                } else if (goal.type == "NUMERIC") {
                    Column {
                        Text("Atualizar Progresso", color = Color.Gray, fontSize = 12.sp)
                        Slider(
                            value = goal.currentValue,
                            onValueChange = { onUpdateProgress(it) },
                            valueRange = 0f..goal.targetValue,
                            colors = SliderDefaults.colors(thumbColor = neonGreen, activeTrackColor = neonGreen)
                        )
                    }
                } else if (goal.type == "CONSISTENCY") {
                    Button(
                        onClick = onIncrementStreak,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = neonGreen.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, neonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Whatshot, null, tint = neonGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("MARCAR PRESENÇA HOJE", color = neonGreen, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Metas de consistência são infinitas. Elas mostram seu esforço contínuo.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (goal.deadline != null) {
                        Text(
                            "Expira em: ${java.text.SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(goal.deadline))}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else { Spacer(Modifier.width(1.dp)) }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyGoalsState(neonGreen: Color) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Flag, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nenhuma meta ativa por aqui.", color = Color.Gray)
        Text("Que tal começar algo novo?", color = neonGreen, fontWeight = FontWeight.Bold)
    }
}
