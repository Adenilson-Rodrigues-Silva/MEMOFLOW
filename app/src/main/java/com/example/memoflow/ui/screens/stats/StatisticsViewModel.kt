package com.example.memoflow.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.local.entity.GratitudeEntity
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

data class MoodStat(val emoji: String, val label: String, val percentage: Int, val color: androidx.compose.ui.graphics.Color, val count: Int = 0)

data class CityHumorStat(val cityName: String, val averageScore: Float, val count: Int, val insight: String = "")

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
    val gratitudeCount: Int = 0,
    val totalGratitudesInPote: Int = 0,
    val monthName: String = "",
    val happiestCity: CityHumorStat? = null,
    val totalCitiesVisited: Int = 0,
    val topCreationPlace: String? = null
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
            
            val monthLabel = currentReferenceDate.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                .replaceFirstChar { it.uppercase() }

            combine(
                repository.getNotesInDateRange(startTimestamp, endTimestamp),
                repository.allGratitudes
            ) { notes, gratitudes ->
                val filteredGratitudes = gratitudes.filter { it.date in startTimestamp..endTimestamp }
                processAllStats(notes, filteredGratitudes, gratitudes.size, startDate, endDate, monthLabel)
            }.collectLatest { 
                _statsData.value = it
            }
        }
    }

    private fun processAllStats(
        notes: List<NoteEntity>, 
        gratitudesInRange: List<GratitudeEntity>,
        totalGratitudes: Int,
        startDate: LocalDate, 
        endDate: LocalDate,
        monthLabel: String
    ): StatsData {
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

        val humorCounts = notes.groupingBy { it.emoji }.eachCount()
        val totalNotes = notes.size.coerceAtLeast(1)
        val topMoods = humorCounts.toList()
            .sortedByDescending { it.second }
            .map { (emoji, count) ->
                MoodStat(
                    emoji = emoji, 
                    label = mapEmojiToLabel(emoji), 
                    percentage = (count * 100) / totalNotes, 
                    color = mapEmojiToColor(emoji),
                    count = count
                )
            }

        // --- Lógica Geográfica Avançada ---
        val citiesGrouped = notes.filter { it.locationName != null }.groupBy { it.locationName!! }
        
        val happiestCity = citiesGrouped.map { (city, cityNotes) ->
            val avg = cityNotes.map { mapEmojiToScore(it.emoji) }.average().toFloat()
            CityHumorStat(
                cityName = city,
                averageScore = avg,
                count = cityNotes.size,
                insight = when {
                    avg >= 4.5 -> "Sua vibração aqui é radiante ✨"
                    avg >= 3.5 -> "Você se sente em equilíbrio aqui."
                    else -> "Um lugar de superação e reflexão."
                }
            )
        }.maxByOrNull { it.averageScore }

        val topCreationPlace = citiesGrouped.maxByOrNull { it.value.size }?.key
        val totalCitiesVisited = citiesGrouped.keys.size

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
            gratitudeCount = gratitudesInRange.size,
            totalGratitudesInPote = totalGratitudes,
            monthName = monthLabel,
            happiestCity = happiestCity,
            totalCitiesVisited = totalCitiesVisited,
            topCreationPlace = topCreationPlace
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
