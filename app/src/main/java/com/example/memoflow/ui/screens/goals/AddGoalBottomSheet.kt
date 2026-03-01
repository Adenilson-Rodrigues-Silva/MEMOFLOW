package com.example.memoflow.ui.screens.goals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memoflow.data.local.entity.GoalEntity
import com.example.memoflow.data.local.entity.GoalStep
import java.text.SimpleDateFormat
import java.util.*

data class SuggestedGoal(
    val title: String,
    val category: String,
    val type: String,
    val target: Float = 0f,
    val unit: String = "",
    val steps: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(
    onDismiss: () -> Unit,
    onGoalCreated: (GoalEntity) -> Unit,
    neonGreen: Color = Color(0xFF00FFC2)
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedCategory by remember { mutableStateOf("Pessoal") }
    var selectedType by remember { mutableStateOf("CONSISTENCY") }
    
    // Estados para o formulário preenchido por sugestão ou manual
    var prepopulatedTitle by remember { mutableStateOf("") }
    var prepopulatedTarget by remember { mutableStateOf("") }
    var prepopulatedUnit by remember { mutableStateOf("") }
    var prepopulatedSteps = remember { mutableStateListOf<GoalStep>() }

    val suggestions = listOf(
        SuggestedGoal("Beber 2L de Água", "Saúde", "NUMERIC", 2000f, "ml"),
        SuggestedGoal("Meditar 10 min", "Saúde", "CONSISTENCY"),
        SuggestedGoal("Escrever no Diário", "Pessoal", "CONSISTENCY"),
        SuggestedGoal("Ler 20 páginas", "Estudo", "NUMERIC", 20f, "pág"),
        SuggestedGoal("Organizar a Semana", "Pessoal", "STEPS", steps = listOf("Definir prioridades", "Limpar mesa", "Ajustar agenda"))
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (step == 1) {
                Text("Sugestões Rápidas", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(suggestions) { sugg ->
                        SuggestionChip(sugg, neonGreen) {
                            prepopulatedTitle = sugg.title
                            selectedCategory = sugg.category
                            selectedType = sugg.type
                            prepopulatedTarget = if(sugg.target > 0) sugg.target.toInt().toString() else ""
                            prepopulatedUnit = sugg.unit
                            prepopulatedSteps.clear()
                            sugg.steps.forEach { prepopulatedSteps.add(GoalStep(title = it)) }
                            step = 2
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("Ou crie do seu jeito", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                CategoryGrid(selectedCategory) { selectedCategory = it }
                Spacer(modifier = Modifier.height(32.dp))

                Text("Qual o formato?", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                TypeOption("Consistência", "Hábitos diários", Icons.Default.Refresh, selectedType == "CONSISTENCY", { selectedType = "CONSISTENCY" }, neonGreen)
                TypeOption("Numérica", "Valores e metas", Icons.Default.AddChart, selectedType == "NUMERIC", { selectedType = "NUMERIC" }, neonGreen)
                TypeOption("Por Etapas", "Projetos e planos", Icons.AutoMirrored.Filled.List, selectedType == "STEPS", { selectedType = "STEPS" }, neonGreen)

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { 
                        prepopulatedTitle = ""
                        prepopulatedTarget = ""
                        prepopulatedUnit = ""
                        prepopulatedSteps.clear()
                        step = 2 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("CONTINUAR MANUAL", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                GoalDetailsForm(
                    category = selectedCategory,
                    type = selectedType,
                    initialTitle = prepopulatedTitle,
                    initialTarget = prepopulatedTarget,
                    initialUnit = prepopulatedUnit,
                    initialSteps = prepopulatedSteps,
                    onBack = { step = 1 },
                    onConfirm = { onGoalCreated(it); onDismiss() },
                    neonGreen = neonGreen
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(sugg: SuggestedGoal, neonGreen: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(sugg.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(sugg.category, color = neonGreen.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}

@Composable
fun CategoryGrid(selected: String, onSelect: (String) -> Unit) {
    val categories = listOf(
        "Saúde" to Icons.Default.Favorite,
        "Estudo" to Icons.Default.Book,
        "Financeiro" to Icons.Default.AccountBalanceWallet,
        "Relacionamento" to Icons.Default.Group,
        "Pessoal" to Icons.Default.Person
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (name, icon) ->
                    CategoryItem(name, icon, selected == name, Modifier.weight(1f)) { onSelect(name) }
                }
                if (row.size < 3) Spacer(modifier = Modifier.weight((3 - row.size).toFloat()))
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFF00FFC2).copy(alpha = 0.1f) else Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00FFC2) else Color.Transparent),
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, tint = if (isSelected) Color(0xFF00FFC2) else Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(name, color = if (isSelected) Color.White else Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun TypeOption(title: String, desc: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, neonGreen: Color) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) neonGreen else Color.Gray.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) neonGreen else Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailsForm(
    category: String,
    type: String,
    initialTitle: String,
    initialTarget: String,
    initialUnit: String,
    initialSteps: List<GoalStep>,
    onBack: () -> Unit,
    onConfirm: (GoalEntity) -> Unit,
    neonGreen: Color
) {
    var title by remember { mutableStateOf(initialTitle) }
    var targetValue by remember { mutableStateOf(initialTarget) }
    var unit by remember { mutableStateOf(initialUnit) }
    var priority by remember { mutableStateOf("Média") }
    var deadline by remember { mutableStateOf<Long?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val steps = remember { mutableStateListOf<GoalStep>().apply { addAll(initialSteps) } }
    var currentStepText by remember { mutableStateOf("") }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    deadline = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK", color = neonGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("CANCELAR", color = Color.White) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
            Text("Detalhes da Meta", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("O que você vai conquistar?") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = neonGreen,
                focusedLabelColor = neonGreen,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                cursorColor = neonGreen
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Prioridade", color = Color.Gray, fontSize = 14.sp)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Baixa", "Média", "Alta").forEach { p ->
                FilterChip(
                    selected = priority == p,
                    onClick = { priority = p },
                    label = { Text(p) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = neonGreen.copy(alpha = 0.2f), selectedLabelColor = neonGreen)
                )
            }
        }

        if (type != "CONSISTENCY") {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = { showDatePicker = true },
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = neonGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (deadline == null) "Definir Prazo (Opcional)" else SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(deadline!!)),
                        color = if (deadline == null) Color.Gray else Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (type == "NUMERIC") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = targetValue, onValueChange = { targetValue = it }, label = { Text("Meta") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonGreen, focusedTextColor = Color.White, cursorColor = neonGreen))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unidade") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonGreen, focusedTextColor = Color.White, cursorColor = neonGreen))
            }
        }

        if (type == "STEPS") {
            Text("Etapas do Plano", color = Color.Gray, fontSize = 14.sp)
            Row {
                OutlinedTextField(value = currentStepText, onValueChange = { currentStepText = it }, placeholder = { Text("Ex: Comprar curso") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonGreen, focusedTextColor = Color.White, cursorColor = neonGreen))
                IconButton(onClick = { if (currentStepText.isNotBlank()) { steps.add(GoalStep(title = currentStepText)); currentStepText = "" } }) { Icon(Icons.Default.Add, null, tint = neonGreen) }
            }
            steps.forEach { step ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Default.Circle, null, tint = neonGreen, modifier = Modifier.size(8.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(step.title, color = Color.White, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                val finalGoal = GoalEntity(
                    title = title,
                    category = category,
                    type = type,
                    targetValue = if (type == "STEPS") steps.size.toFloat() else targetValue.toFloatOrNull() ?: 0f,
                    unit = unit,
                    priority = priority,
                    deadline = deadline,
                    steps = steps.toList()
                )
                onConfirm(finalGoal)
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("CRIAR META", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
