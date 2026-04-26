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
import com.example.memoflow.utils.AiPrefs
import com.example.memoflow.utils.BillingPrefs
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val error: String? = null,
    val currentScope: String = "", // "", "today", "weekly", "monthly"
    val dailyCounts: Map<String, Int> = mapOf("today" to 0, "weekly" to 0, "monthly" to 0),
    val nextAvailableTime: Map<String, Long> = emptyMap() // Timestamp em ms
)

data class StatsData(
    val isWeekly: Boolean = true,
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
    private val billingPrefs: BillingPrefs,
    private val aiPrefs: AiPrefs
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
        loadAiPersistence()
    }

    private fun loadAiPersistence() {
        viewModelScope.launch {
            val counts = aiPrefs.getDailyCounts()
            val times = aiPrefs.getNextAvailableTimes()
            _statsData.value = _statsData.value.copy(
                aiInsight = _statsData.value.aiInsight.copy(
                    dailyCounts = counts,
                    nextAvailableTime = times
                )
            )
        }
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

    private var isWeeklyView = true

    fun toggleView() {
        isWeeklyView = !isWeeklyView
        loadStats()
    }

    fun generateAiInsights(scope: String = "auto") {
        val finalScope = if (scope == "auto") {
            if (isMonthly) "monthly" else "weekly"
        } else scope
        
        val now = System.currentTimeMillis()
        val currentInsight = _statsData.value.aiInsight
        val nextTime = currentInsight.nextAvailableTime[finalScope] ?: 0L
        val count = currentInsight.dailyCounts[finalScope] ?: 0

        if (count >= 12) {
            _statsData.value = _statsData.value.copy(
                aiInsight = currentInsight.copy(error = "Limite diário de 12 gerações atingido para este modo. Libera à meia-noite.")
            )
            return
        }

        if (now < nextTime) {
            val remainingSec = (nextTime - now) / 1000
            _statsData.value = _statsData.value.copy(
                aiInsight = currentInsight.copy(error = "Aguarde ${remainingSec / 60}min ${remainingSec % 60}s para gerar novamente.")
            )
            return
        }

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
                // Determina o intervalo de datas real para o escopo solicitado
                val zoneId = ZoneId.systemDefault()
                val nowTime = LocalDate.now()
                val (startRange, endRange) = when (finalScope) {
                    "today" -> nowTime to nowTime
                    "weekly" -> nowTime.with(java.time.DayOfWeek.MONDAY) to nowTime.with(java.time.DayOfWeek.SUNDAY)
                    "monthly" -> nowTime.withDayOfMonth(1) to nowTime.withDayOfMonth(nowTime.lengthOfMonth())
                    else -> nowTime to nowTime
                }

                val startMillis = startRange.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val endMillis = endRange.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()

                // Busca as notas específicas para este escopo direto do repositório
                val allNotesInRange = repository.getNotesByDateRange(startMillis, endMillis).first()
                
                // Filtra notas por privacidade (Cadeado e Cápsula)
                val targetNotes = allNotesInRange.filter { note ->
                    val now = System.currentTimeMillis()
                    val isHiddenByLock = note.isLocked
                    val isHiddenByCapsule = note.isTimeCapsule && (note.unlockDate ?: 0L) > now
                    
                    !isHiddenByLock && !isHiddenByCapsule
                }

                val cleanNotes = targetNotes.map { note ->
                    val plainText = note.contentHtml.replace(Regex("<[^>]*>"), " ").trim()
                    val date = Instant.ofEpochMilli(note.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    "Data: $date | Humor: ${note.emoji}\nNota: $plainText"
                }.joinToString("\n---\n")

                if (cleanNotes.isBlank()) {
                    val msg = when {
                        allNotesInRange.isNotEmpty() && targetNotes.isEmpty() -> {
                            "Suas notas deste período estão protegidas (trancadas ou em cápsulas) e a IA não tem permissão para lê-las."
                        }
                        finalScope == "today" -> "Você ainda não escreveu nada hoje!"
                        finalScope == "weekly" -> "Nenhuma nota encontrada nesta semana!"
                        finalScope == "monthly" -> "Nenhuma nota encontrada este mês!"
                        else -> "Escreva algumas notas primeiro!"
                    }
                    _statsData.value = _statsData.value.copy(
                        aiInsight = AiInsightData(isLoading = false, error = msg)
                    )
                    return@launch
                }

                val promptTask = when (finalScope) {
                    "today" -> "Analise meu dia de hoje de forma ultra-focada. Como eu me senti, qual foi o ponto alto e o que posso fazer para que amanhã seja ainda melhor?"
                    "weekly" -> "Analise minha SEMANA. Identifique a evolução emocional, os principais gatilhos (positivos ou negativos) e como meu humor oscilou entre os dias."
                    "monthly" -> "Realize uma retrospectiva profunda e analítica do meu MÊS. Identifique padrões comportamentais recorrentes, flutuações de humor significativas e como meus sentimentos evoluíram da primeira para a última semana. Procure conexões entre os eventos relatados e forneça uma visão macro sobre meu crescimento e estado mental neste período."
                    else -> "Analise meu período atual. Quais os principais sentimentos e padrões observados?"
                }

                val specificInstruction = when (finalScope) {
                    "today" -> "O RESUMO deve ter EXATAMENTE 3 frases curtas e diretas sobre o dia de hoje."
                    "weekly" -> "O RESUMO deve ter de 3 a 4 frases detalhando a experiência da semana."
                    "monthly" -> "O RESUMO deve ser um parágrafo denso e detalhado (5 a 8 frases), conectando os fatos do mês e oferecendo um insight profundo."
                    else -> "O RESUMO deve ser conciso e acolhedor."
                }

                val prompt = """
                    Você é o MemoFlow AI, um analista emocional empático e perspicaz.
                    
                    SUA TAREFA:
                    $promptTask
                    
                    REGRAS OBRIGATÓRIAS:
                    1. Responda em Português do Brasil.
                    2. $specificInstruction
                    3. No campo SENTIMENTOS, forneça uma lista de EXATAMENTE 7 números (0.0 a 1.0) representando a evolução do humor no período.
                    
                    FORMATO DE RESPOSTA (ESTRITAMENTE NESTE PADRÃO):
                    RESUMO: [Seu texto aqui]
                    SENTIMENTOS: [num1, num2, num3, num4, num5, num6, num7]
                    
                    NOTAS DO USUÁRIO PARA ANÁLISE:
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

                val newCounts = currentInsight.dailyCounts.toMutableMap()
                newCounts[finalScope] = (newCounts[finalScope] ?: 0) + 1
                
                val newNextTimes = currentInsight.nextAvailableTime.toMutableMap()
                newNextTimes[finalScope] = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutos

                aiPrefs.saveDailyCounts(newCounts)
                aiPrefs.saveNextAvailableTimes(newNextTimes)

                _statsData.value = _statsData.value.copy(
                    aiInsight = AiInsightData(
                        summary = summary,
                        sentimentScores = sentimentScores,
                        isLoading = false,
                        currentScope = finalScope,
                        dailyCounts = newCounts,
                        nextAvailableTime = newNextTimes
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
            _statsData.value = _statsData.value.copy(isWeekly = !isMonthly)
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
        val daysInRange = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val moodPoints = mutableListOf<Float>()
        val labels = MutableList(daysInRange) { "" }
        val entriesPerDay = MutableList(daysInRange) { 0 }
        
        val notesByDate = notes.groupBy { 
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() 
        }

        var lastValidScore = 0.5f
        for (i in 0 until daysInRange) {
            val date = startDate.plusDays(i.toLong())
            labels[i] = if (daysInRange <= 7) {
                date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("pt", "BR"))
                    .replace(".", "").uppercase()
            } else {
                date.dayOfMonth.toString()
            }
            
            val dayNotes = notesByDate[date] ?: emptyList()
            entriesPerDay[i] = dayNotes.size
            
            if (dayNotes.isNotEmpty()) {
                val avgScore = dayNotes.map { mapEmojiToScore(it.emoji) }.average().toFloat()
                moodPoints.add(avgScore)
                lastValidScore = avgScore
            } else {
                moodPoints.add(lastValidScore)
            }
        }

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
        "😊", "🥰", "🤩", "🥳", "💗" -> 0.93f
        "😄", "😆", "😁", "✨" -> 0.79f
        "🙂", "☺️", "😋" -> 0.64f
        "😐", "😶", "🫠", "🧐" -> 0.50f
        "😕", "😟", "🙁", "🥱" -> 0.36f
        "😔", "😞", "😣", "😴" -> 0.21f
        "😢", "😭", "😠", "😡", "💔" -> 0.07f
        else -> 0.50f
    }

    private fun mapEmojiToLabel(emoji: String): String = when(emoji) {
        "😊", "🥰", "🤩", "🥳" -> "Incrível"
        "😄", "😆", "😁" -> "Radiante"
        "🙂", "☺️" -> "Bem"
        "😐", "😶" -> "Neutro"
        "😕", "😟" -> "Inquieto"
        "😔", "😞" -> "Triste"
        "😢", "😭" -> "Mal"
        "😠", "😡" -> "Bravo"
        else -> "Outro"
    }

    private fun mapEmojiToColor(emoji: String): Color = when(emoji) {
        "😊", "🥰", "🤩", "🥳", "💗", "😄", "😆", "😁", "✨" -> Color(0xFF00FFC2)
        "🙂", "☺️", "😋", "😐", "😶", "🫠", "🧐" -> Color(0xFFFFD700)
        "😕", "😟", "🙁", "🥱", "😔", "😞", "😣", "😴", "😢", "😭", "😠", "😡", "💔" -> Color(0xFFFF4D4D)
        else -> Color.Gray
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MemoApplication
                return StatisticsViewModel(application.repository, application.billingPrefs, application.aiPrefs) as T
            }
        }
    }
}
