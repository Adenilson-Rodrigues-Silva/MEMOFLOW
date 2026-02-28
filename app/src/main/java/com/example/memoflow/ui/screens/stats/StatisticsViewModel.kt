package com.example.memoflow.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.*

data class MoodStat(val emoji: String, val label: String, val percentage: Int, val color: androidx.compose.ui.graphics.Color)

data class StatsData(
    val moodPoints: List<Float> = emptyList(),
    val topMoods: List<MoodStat> = emptyList(),
    val streak: Int = 0,
    val bestDay: String = "-",
    val worstDay: String = "-",
    val totalWords: Int = 0,
    val entriesPerDay: List<Int> = emptyList(),
    val peakPeriod: String = "Noite",
    val audioCount: Int = 0,
    val imageCount: Int = 0,
    val dayLabels: List<String> = emptyList()
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
            
            // LÓGICA DE SEMANA/MÊS FIXO (PROFISSIONAL)
            val (startDate, endDate) = if (!isMonthly) {
                // Segunda a Domingo
                val start = currentReferenceDate.with(java.time.DayOfWeek.MONDAY)
                val end = start.plusDays(6)
                start to end
            } else {
                // Início ao fim do mês
                val start = currentReferenceDate.withDayOfMonth(1)
                val end = currentReferenceDate.withDayOfMonth(currentReferenceDate.lengthOfMonth())
                start to end
            }

            val startTimestamp = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endTimestamp = endDate.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()
            
            repository.getNotesInDateRange(startTimestamp, endTimestamp).collectLatest { notes ->
                processNotes(notes, startDate, endDate)
            }
        }
    }

    private fun processNotes(notes: List<NoteEntity>, startDate: LocalDate, endDate: LocalDate) {
        val zoneId = ZoneId.systemDefault()
        val notesByDay = notes.groupBy { 
            Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
        }

        val moodPoints = mutableListOf<Float>()
        val entries = mutableListOf<Int>()
        val labels = mutableListOf<String>()
        
        var tempDate = startDate
        while (!tempDate.isAfter(endDate)) {
            val dayNotes = notesByDay[tempDate] ?: emptyList()
            entries.add(dayNotes.size)
            
            val dayName = tempDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).uppercase().take(3)
            labels.add(dayName)
            
            if (dayNotes.isNotEmpty()) {
                moodPoints.add(dayNotes.map { mapEmojiToScore(it.emoji) }.average().toFloat())
            } else {
                moodPoints.add(3f) // Linha reta no neutro se não houver dados
            }
            tempDate = tempDate.plusDays(1)
        }

        // Top Humores
        val humorCounts = notes.groupingBy { it.emoji }.eachCount()
        val totalNotes = notes.size.coerceAtLeast(1)
        val topMoods = if (notes.isEmpty()) emptyList() else humorCounts.toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { (emoji, count) ->
                MoodStat(emoji, mapEmojiToLabel(emoji), (count * 100) / totalNotes, mapEmojiToColor(emoji))
            }

        // Streak (Sempre baseado no hoje real)
        var streak = 0
        var checkDay = LocalDate.now()
        // ... lógica de streak simplificada
        
        val words = notes.sumOf { it.contentHtml.replace(Regex("<[^>]*>"), "").trim().split("\\s+".toRegex()).size }

        _statsData.value = StatsData(
            moodPoints = moodPoints,
            topMoods = topMoods,
            streak = streak,
            totalWords = if (notes.isEmpty()) 0 else words,
            entriesPerDay = entries,
            dayLabels = labels,
            audioCount = notes.count { it.audioPath != null },
            imageCount = notes.sumOf { it.images.size }
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
