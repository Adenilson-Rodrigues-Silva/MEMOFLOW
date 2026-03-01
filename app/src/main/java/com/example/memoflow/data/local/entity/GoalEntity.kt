package com.example.memoflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Saúde, Estudo, Financeiro, Relacionamento, Pessoal
    val priority: String = "Média", // Baixa, Média, Alta
    val type: String, // CONSISTENCY, NUMERIC, STEPS
    val targetValue: Float = 0f,
    val currentValue: Float = 0f,
    val unit: String = "", // ex: €, km, dias
    val deadline: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val steps: List<GoalStep> = emptyList(),
    val nextStepSuggestion: String = ""
)

data class GoalStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)
