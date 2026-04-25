package com.example.memoflow.ui.screens.stats

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.MemoApplication
import com.example.memoflow.data.local.entity.GratitudeEntity
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.repository.MemoRepository
import com.example.memoflow.utils.BillingPrefs
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.TimeUnit

data class MoodStat(
    val emoji: String,
    val label: String,
    val percentage: Int,
    val color: Color,
    val count: Int
)

data class CityHumorStat(
    val cityName: String,
    val averageScore: Float,
    val count: Int,
    val insight: String
)

data class AiInsightData(
    val summary: String = "",
    val sentimentScores: List<Float> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

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
    val topCreationPlace: String? = null,
    val aiInsight: AiInsightData = AiInsightData()
)

class StatisticsViewModel(
    private val repository: MemoRepository,
    private val billingPrefs: BillingPrefs
) : ViewModel() {

    private val _statsData = MutableStateFlow(StatsData())
    val statsData: StateFlow<StatsData> = _statsData.asStateFlow()

    val isPremium: StateFlow<Boolean> = billingPrefs.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var isMonthly = true
    private var currentReferenceDate = LocalDate.now()
    private var currentNotesForAi: List<NoteEntity> = emptyList()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    init {
        loadStats()
    }

    fun setPeriod(monthly: Boolean) {
        isMonthly = monthly
        loadStats()
    }

    fun setPeriodInt(index: Int) {
        isMonthly = index == 1
        loadStats()
    }

    fun setReferenceDate(date: LocalDate) {
        currentReferenceDate = date
        loadStats()
    }

    fun generateAiInsights() {
        viewModelScope.launch {
            val key = com.example.memoflow.BuildConfig.GROQ_API_KEY
            
            if (key.isBlank() || key == "COLE_SUA_CHAVE_DO_GROQ_AQUI") {
                _statsData.value = _statsData.value.copy(
                    aiInsight = AiInsightData(error = "Chave do Groq não configurada no local.properties")
                )
                return@launch
            }

            _statsData.value = _statsData.value.copy(
                aiInsight = _statsData.value.aiInsight.copy(isLoading = true, error = null)
            )

            try {
                val cleanNotes = currentNotesForAi.map { note ->
                    val plainText = note.contentHtml.replace(Regex("<[^>]*>"), " ").trim()
                    "Data: ${Instant.ofEpochMilli(note.date).atZone(ZoneId.systemDefault()).toLocalDate()}\nNota: $plainText"
                }.joinToString("\n---\n")

                if (cleanNotes.isBlank()) {
                    _statsData.value = _statsData.value.copy(
                        aiInsight = AiInsightData(isLoading = false, error = "Escreva algumas notas primeiro!")
                    )
                    return@launch
                }

                val prompt = """
                    Analise estas notas de diário e responda estritamente neste formato:
                    RESUMO: [Resumo motivador de 3 frases em Português]
                    SENTIMENTOS: [Lista de 7 números entre 0.0 e 1.0]
                    
                    Notas:
                    $cleanNotes
                """.trimIndent()

                val requestBody = mapOf(
                    "model" to "llama-3.1-8b-instant",
                    "messages" to listOf(
                        mapOf("role" to "user", "content" to prompt)
                    ),
                    "temperature" to 0.7
                )

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = gson.toJson(requestBody).toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $key")
                    .post(body)
                    .build()

                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    throw Exception("Erro Groq: ${response.code} - $responseBody")
                }

                val jsonResponse = gson.fromJson(responseBody, Map::class.java)
                val choices = jsonResponse["choices"] as List<*>
                val firstChoice = choices[0] as Map<*, *>
                val message = firstChoice["message"] as Map<*, *>
                val content = message["content"] as String

                val summary = content.substringAfter("RESUMO:").substringBefore("SENTIMENTOS:").trim()
                val sentimentString = content.substringAfter("SENTIMENTOS:").trim()
                    .replace("[", "").replace("]", "")
                
                val sentimentScores = sentimentString.split(",")
                    .mapNotNull { it.trim().toFloatOrNull() }

                _statsData.value = _statsData.value.copy(
                    aiInsight = AiInsightData(
                        summary = summary,
                        sentimentScores = sentimentScores,
                        isLoading = false
                    )
                )

            } catch (e: Exception) {
                Log.e("StatisticsViewModel", "Erro Groq", e)
                _statsData.value = _statsData.value.copy(
                    aiInsight = AiInsightData(isLoading = false, error = "Falha na conexão com a IA. Verifique sua chave e internet.")
                )
            }
        }
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

            val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = endDate.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()

            repository.getNotesByDateRange(startMillis, endMillis).collect { notes ->
                currentNotesForAi = notes
                repository.getGratitudesByDateRange(startMillis, endMillis).collect { gratitudes ->
                    val totalGratitudes = repository.getTotalGratitudeCountSync()
                    val streak = repository.getCurrentStreakSync()
                    
                    _statsData.value = processAllStats(
                        notes, 
                        gratitudes, 
                        streak, 
                        startDate, 
                        endDate,
                        currentReferenceDate.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                            .replaceFirstChar { it.uppercase() },
                        totalGratitudes
                    )
                }
            }
        }
    }

    private fun processAllStats(
        notes: List<NoteEntity>,
        gratitudes: List<GratitudeEntity>,
        streak: Int,
        startDate: LocalDate,
        endDate: LocalDate,
        monthName: String,
        totalGratitudesInPote: Int
    ): StatsData {
        val moodPoints = notes.sortedBy { it.date }.map { mapEmojiToScore(it.emoji) }
        val moodCounts = notes.groupBy { it.emoji }.mapValues { it.value.size }
        val totalNotes = notes.size.coerceAtLeast(1)
        
        val topMoods = moodCounts.map { (emoji, count) ->
            MoodStat(
                emoji = emoji,
                label = mapEmojiToLabel(emoji),
                count = count,
                percentage = (count * 100) / totalNotes,
                color = mapEmojiToColor(emoji)
            )
        }.sortedByDescending { it.count }.take(4)

        val daysInRange = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val entriesPerDay = MutableList(daysInRange) { 0 }
        val labels = MutableList(daysInRange) { "" }
        
        for (i in 0 until daysInRange) {
            val date = startDate.plusDays(i.toLong())
            labels[i] = date.dayOfMonth.toString()
            entriesPerDay[i] = notes.count { 
                Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() == date 
            }
        }

        val cities = notes.mapNotNull { it.locationName }.groupBy { it }
        val happiestCity = cities.map { (name, _) ->
            val cityNotes = notes.filter { it.locationName == name }
            val avg = cityNotes.map { mapEmojiToScore(it.emoji) }.average().toFloat()
            CityHumorStat(name, avg, cityNotes.size, "Cidade com boas vibrações!")
        }.maxByOrNull { it.averageScore }

        val lockedDays = notes.filter { it.isLocked }.map { 
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() 
        }
        val capsuleDays = notes.filter { it.isTimeCapsule }.map { 
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() 
        }

        return StatsData(
            moodPoints = moodPoints,
            topMoods = topMoods,
            streak = streak,
            entriesPerDay = entriesPerDay,
            dayLabels = labels,
            audioCount = notes.count { it.audioPath != null },
            imageCount = notes.count { it.images.isNotEmpty() },
            lockedCount = notes.count { it.isLocked },
            capsuleCount = notes.count { it.isTimeCapsule },
            lockedDays = lockedDays,
            capsuleDays = capsuleDays,
            gratitudeCount = gratitudes.size,
            totalGratitudesInPote = totalGratitudesInPote,
            monthName = monthName,
            happiestCity = happiestCity,
            totalCitiesVisited = cities.size,
            aiInsight = _statsData.value.aiInsight
        )
    }

    private fun mapEmojiToScore(emoji: String): Float = when(emoji) {
        "😊", "😄", "🥰" -> 1.0f
        "🙂", "😐" -> 0.6f
        "😔", "😢", "😠" -> 0.2f
        else -> 0.5f
    }

    private fun mapEmojiToLabel(emoji: String): String = when(emoji) {
        "😊" -> "Feliz"
        "😄" -> "Radiante"
        "🥰" -> "Amado"
        "🙂" -> "Bem"
        "😐" -> "Neutro"
        "😔" -> "Triste"
        "😢" -> "Mal"
        "😠" -> "Bravo"
        else -> "Outro"
    }

    private fun mapEmojiToColor(emoji: String): Color = when(emoji) {
        "😊", "😄", "🥰" -> Color(0xFF00FFC2)
        "🙂", "😐" -> Color(0xFFFFD700)
        "😔", "😢", "😠" -> Color(0xFFFF4D4D)
        else -> Color.Gray
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MemoApplication
                return StatisticsViewModel(application.repository, application.billingPrefs) as T
            }
        }
    }
}
