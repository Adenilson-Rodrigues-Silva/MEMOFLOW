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

    init {
        setPeriod(0) // Começa com Semanal
    }

    fun setPeriod(tabIndex: Int) {
        val daysCount = if (tabIndex == 0) 7 else 30
        loadStats(daysCount)
    }

    private fun loadStats(daysCount: Int) {
        viewModelScope.launch {
            val endDate = System.currentTimeMillis()
            val startDate = endDate - (daysCount.toLong() * 24 * 60 * 60 * 1000) 
            repository.getNotesInDateRange(startDate, endDate).collectLatest { notes ->
                processNotes(notes, daysCount)
            }
        }
    }

    private fun processNotes(notes: List<NoteEntity>, daysCount: Int) {
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        val notesByDay = notes.groupBy { 
            Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
        }

        val moodPoints = mutableListOf<Float>()
        val entries = mutableListOf<Int>()
        val labels = mutableListOf<String>()
        val moodScoresByDayName = mutableMapOf<String, MutableList<Float>>()

        // Iterar pelo período selecionado
        for (i in (daysCount - 1) downTo 0) {
            val day = today.minusDays(i.toLong())
            val dayNotes = notesByDay[day] ?: emptyList()
            
            entries.add(dayNotes.size)
            
            val dayName = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).uppercase().take(3)
            labels.add(dayName)
            
            val scores = dayNotes.map { mapEmojiToScore(it.emoji) }
            if (scores.isNotEmpty()) {
                moodPoints.add(scores.average().toFloat())
                moodScoresByDayName.getOrPut(dayName) { mutableListOf() }.addAll(scores)
            } else {
                moodPoints.add(3f) // Neutro se não houver nota
            }
        }

        // Top Humores
        val humorCounts = notes.groupingBy { it.emoji }.eachCount()
        val totalNotes = notes.size.coerceAtLeast(1)
        val topMoods = humorCounts.toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { (emoji, count) ->
                MoodStat(emoji, mapEmojiToLabel(emoji), (count * 100) / totalNotes, mapEmojiToColor(emoji))
            }

        // Melhor/Pior Dia
        val avgMoodByDayName = moodScoresByDayName.mapValues { it.value.average().toFloat() }
        val bestDay = avgMoodByDayName.maxByOrNull { it.value }?.key ?: "-"

        // Streak
        var streak = 0
        var checkDay = today
        while (notesByDay.containsKey(checkDay)) {
            streak++; checkDay = checkDay.minusDays(1)
        }

        val words = notes.sumOf { it.contentHtml.replace(Regex("<[^>]*>"), "").trim().split("\\s+".toRegex()).size }

        _statsData.value = StatsData(
            moodPoints = moodPoints,
            topMoods = topMoods,
            streak = streak,
            bestDay = bestDay,
            totalWords = words,
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
