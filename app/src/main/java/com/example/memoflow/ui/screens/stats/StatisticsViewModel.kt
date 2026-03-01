package com.example.memoflow.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.data.local.entity.GoalEntity
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import java.time.format.TextStyle
import java.util.*

data class MoodStat(val emoji: String, val label: String, val percentage: Int, val color: androidx.compose.ui.graphics.Color)

data class GoalStat(val category: String, val count: Int, val color: androidx.compose.ui.graphics.Color)

data class StatsData(
    val moodPoints: List<Float> = emptyList(),
    val topMoods: List<MoodStat> = emptyList(),
    val streak: Int = 0,
    val entriesPerDay: List<Int> = emptyList(),
    val audioCount: Int = 0,
    val imageCount: Int = 0,
    val dayLabels: List<String> = emptyList(),
    val lockedCount: Int = 0,
    val capsuleCount: Int = 0,
    val lockedDays: List<LocalDate> = emptyList(),
    val capsuleDays: List<LocalDate> = emptyList(),
    
    // Novos campos de Metas
    val goalsCompleted: Int = 0,
    val goalsActive: Int = 0,
    val topGoalCategory: String = "-",
    val goalCompletionRate: Float = 0f,
    val categoryDistribution: List<GoalStat> = emptyList()
)

class StatisticsViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _statsData = MutableStateFlow(StatsData())
    val statsData: StateFlow<StatsData> = _statsData.asStateFlow()

    private var isMonthly: Boolean = false
    private var currentReferenceDate: LocalDate = LocalDate.now()

    init {
        loadStats()
    }

    fun setPeriod(tabIndex: Int) {
        isMonthly = tabIndex == 1
        loadStats()
    }

    fun setReferenceDate(date: LocalDate) {
        currentReferenceDate = date
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val zoneId = ZoneId.systemDefault()
            val (startDate, endDate) = if (!isMonthly) {
                val start = currentReferenceDate.with(java.time.DayOfWeek.MONDAY)
                val end = start.plusDays(6)
                start to end
            } else {
                val start = currentReferenceDate.withDayOfMonth(1)
                val end = currentReferenceDate.withDayOfMonth(currentReferenceDate.lengthOfMonth())
                start to end
            }

            val startTimestamp = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endTimestamp = endDate.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()
            
            combine(
                repository.getNotesInDateRange(startTimestamp, endTimestamp),
                repository.allGoals
            ) { notes, goals ->
                processAllStats(notes, goals, startDate, endDate)
            }.collectLatest { 
                _statsData.value = it
            }
        }
    }

    private fun processAllStats(notes: List<NoteEntity>, goals: List<GoalEntity>, startDate: LocalDate, endDate: LocalDate): StatsData {
        val zoneId = ZoneId.systemDefault()
        val notesByDay = notes.groupBy { 
            Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
        }

        val moodPoints = mutableListOf<Float>()
        val entries = mutableListOf<Int>()
        val labels = mutableListOf<String>()
        val lockedDays = mutableSetOf<LocalDate>()
        val capsuleDays = mutableSetOf<LocalDate>()
        
        var tempDate = startDate
        while (!tempDate.isAfter(endDate)) {
            val dayNotes = notesByDay[tempDate] ?: emptyList()
            entries.add(dayNotes.size)
            labels.add(tempDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).uppercase().take(3))
            
            if (dayNotes.isNotEmpty()) {
                moodPoints.add(dayNotes.map { mapEmojiToScore(it.emoji) }.average().toFloat())
                if (dayNotes.any { it.isLocked }) lockedDays.add(tempDate)
                if (dayNotes.any { it.isTimeCapsule }) capsuleDays.add(tempDate)
            } else {
                moodPoints.add(3f) 
            }
            tempDate = tempDate.plusDays(1)
        }

        // Processamento de Humores
        val humorCounts = notes.groupingBy { it.emoji }.eachCount()
        val totalNotes = notes.size.coerceAtLeast(1)
        val topMoods = if (notes.isEmpty()) emptyList() else humorCounts.toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { (emoji, count) ->
                MoodStat(emoji, mapEmojiToLabel(emoji), (count * 100) / totalNotes, mapEmojiToColor(emoji))
            }

        // Processamento de Metas
        val completedGoals = goals.count { it.isCompleted }
        val activeGoals = goals.count { !it.isCompleted }
        val totalGoals = goals.size.coerceAtLeast(1)
        val categoryCounts = goals.groupingBy { it.category }.eachCount()
        val topCategory = categoryCounts.maxByOrNull { it.value }?.key ?: "-"
        
        val categoryStats = categoryCounts.map { (cat, count) ->
            GoalStat(cat, count, mapCategoryToColor(cat))
        }.sortedByDescending { it.count }

        return StatsData(
            moodPoints = moodPoints,
            topMoods = topMoods,
            entriesPerDay = entries,
            dayLabels = labels,
            audioCount = notes.count { it.audioPath != null },
            imageCount = notes.sumOf { it.images.size },
            lockedCount = notes.count { it.isLocked },
            capsuleCount = notes.count { it.isTimeCapsule },
            lockedDays = lockedDays.toList().sorted(),
            capsuleDays = capsuleDays.toList().sorted(),
            
            // Metas
            goalsCompleted = completedGoals,
            goalsActive = activeGoals,
            topGoalCategory = topCategory,
            goalCompletionRate = (completedGoals.toFloat() / totalGoals) * 100,
            categoryDistribution = categoryStats
        )
    }

    private fun mapEmojiToScore(emoji: String) = when(emoji) {
        "🤩" -> 5f; "😊" -> 4f; "😐" -> 3f; "😢" -> 2f; "😭" -> 1f; "😡" -> 1f; "😫" -> 1f; else -> 3f
    }
    private fun mapEmojiToLabel(emoji: String) = when(emoji) {
        "🤩" -> "Incrível"; "😊" -> "Feliz"; "😐" -> "Neutro"; "😢" -> "Triste"; "😭" -> "Mal"; "😡" -> "Bravo"; "😫" -> "Exausto"; else -> "Neutro"
    }
    private fun mapEmojiToColor(emoji: String) = when(emoji) {
        "🤩", "😊" -> androidx.compose.ui.graphics.Color(0xFF00FFC2)
        "😐" -> androidx.compose.ui.graphics.Color(0xFFBB86FC)
        else -> androidx.compose.ui.graphics.Color(0xFFCF6679)
    }

    private fun mapCategoryToColor(cat: String) = when(cat) {
        "Saúde" -> androidx.compose.ui.graphics.Color(0xFF81C784)
        "Estudo" -> androidx.compose.ui.graphics.Color(0xFF64B5F6)
        "Financeiro" -> androidx.compose.ui.graphics.Color(0xFFFFF176)
        "Relacionamento" -> androidx.compose.ui.graphics.Color(0xFFF06292)
        else -> androidx.compose.ui.graphics.Color(0xFF80DEEA)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.memoflow.MemoApplication
                return StatisticsViewModel(application.repository) as T
            }
        }
    }
}
