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
import java.util.*

data class StatsData(
    val moodPoints: List<Float> = emptyList(),
    val moodDistribution: Map<String, Int> = emptyMap(),
    val audioCount: Int = 0,
    val textCount: Int = 0,
    val entriesPerDay: List<Int> = emptyList()
)

class StatisticsViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _statsData = MutableStateFlow(StatsData())
    val statsData: StateFlow<StatsData> = _statsData.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val endDate = System.currentTimeMillis()
            val startDate = endDate - (7 * 24 * 60 * 60 * 1000) 
            
            repository.getNotesInDateRange(startDate, endDate).collectLatest { notes ->
                processNotes(notes)
            }
        }
    }

    private fun processNotes(notes: List<NoteEntity>) {
        val moodPoints = mutableListOf<Float>()
        val distribution = mutableMapOf<String, Int>()
        var audio = 0
        var text = 0
        val entries = IntArray(7) { 0 }

        val today = LocalDate.now()
        
        val notesByDay = notes.groupBy { 
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        for (i in 6 downTo 0) {
            val day = today.minusDays(i.toLong())
            val dayNotes = notesByDay[day] ?: emptyList()
            
            entries[6-i] = dayNotes.size
            
            val avgMood = if (dayNotes.isNotEmpty()) {
                dayNotes.map { mapEmojiToScore(it.emoji) }.average().toFloat()
            } else {
                3f 
            }
            moodPoints.add(avgMood)
            
            dayNotes.forEach { note ->
                distribution[note.humor] = distribution.getOrDefault(note.humor, 0) + 1
                if (note.audioPath != null) audio++
                if (note.contentHtml.replace(Regex("<[^>]*>"), "").isNotBlank()) text++
            }
        }

        _statsData.value = StatsData(
            moodPoints = moodPoints,
            moodDistribution = distribution,
            audioCount = audio,
            textCount = text,
            entriesPerDay = entries.toList()
        )
    }

    private fun mapEmojiToScore(emoji: String): Float {
        return when (emoji) {
            "😭" -> 1f 
            "😢" -> 2f 
            "😐" -> 3f 
            "😊" -> 4f 
            "🤩" -> 5f 
            "😡" -> 1f 
            "😫" -> 1f
            else -> 3f
        }
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
