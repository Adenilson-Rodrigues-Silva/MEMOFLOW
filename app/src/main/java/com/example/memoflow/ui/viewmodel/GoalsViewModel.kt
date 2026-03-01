package com.example.memoflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.data.local.entity.GoalEntity
import com.example.memoflow.data.local.entity.GoalStep
import com.example.memoflow.data.repository.MemoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GoalsUiState(
    val goals: List<GoalEntity> = emptyList(),
    val filter: GoalFilter = GoalFilter.ACTIVE,
    val isLoading: Boolean = false
)

enum class GoalFilter {
    ACTIVE, COMPLETED, OVERDUE, IMPORTANT
}

class GoalsViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _filter = MutableStateFlow(GoalFilter.ACTIVE)
    val filter = _filter.asStateFlow()

    val goalsState: StateFlow<GoalsUiState> = combine(
        repository.allGoals,
        _filter
    ) { allGoals, currentFilter ->
        val filteredGoals = when (currentFilter) {
            GoalFilter.ACTIVE -> allGoals.filter { !it.isCompleted }
            GoalFilter.COMPLETED -> allGoals.filter { it.isCompleted }
            GoalFilter.OVERDUE -> allGoals.filter { !it.isCompleted && it.deadline != null && it.deadline < System.currentTimeMillis() }
            GoalFilter.IMPORTANT -> allGoals.filter { it.priority == "Alta" }
        }
        GoalsUiState(goals = filteredGoals, filter = currentFilter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState())

    val stats = repository.allGoals.map { all ->
        val completed = all.count { it.isCompleted }
        val total = all.size
        val rate = if (total > 0) (completed.toFloat() / total) * 100 else 0f
        
        // Simulação de streak baseada em metas concluídas recentemente
        val activeStreak = if (completed > 0) 1 else 0 
        
        GoalStats(
            completedThisMonth = completed,
            activeStreak = activeStreak,
            progressRate = rate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalStats())

    fun setFilter(filter: GoalFilter) {
        _filter.value = filter
    }

    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun incrementStreak(goal: GoalEntity) {
        val newStreak = goal.streak + 1
        updateGoal(goal.copy(
            streak = newStreak,
            lastUpdated = System.currentTimeMillis()
        ))
    }

    fun toggleStep(goal: GoalEntity, stepId: String) {
        val updatedSteps = goal.steps.map {
            if (it.id == stepId) it.copy(isCompleted = !it.isCompleted) else it
        }
        val completedSteps = updatedSteps.count { it.isCompleted }
        val isNowCompleted = completedSteps == updatedSteps.size && updatedSteps.isNotEmpty()
        
        updateGoal(goal.copy(
            steps = updatedSteps,
            currentValue = completedSteps.toFloat(),
            targetValue = updatedSteps.size.toFloat(),
            isCompleted = isNowCompleted,
            lastUpdated = System.currentTimeMillis()
        ))
    }

    fun updateNumericProgress(goal: GoalEntity, newValue: Float) {
        val isNowCompleted = newValue >= goal.targetValue
        updateGoal(goal.copy(
            currentValue = newValue,
            isCompleted = isNowCompleted,
            lastUpdated = System.currentTimeMillis()
        ))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.memoflow.MemoApplication
                return GoalsViewModel(application.repository) as T
            }
        }
    }
}

data class GoalStats(
    val completedThisMonth: Int = 0,
    val activeStreak: Int = 0,
    val progressRate: Float = 0f
)
