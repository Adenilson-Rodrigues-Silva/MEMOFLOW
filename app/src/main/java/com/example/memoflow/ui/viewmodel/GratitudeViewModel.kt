package com.example.memoflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.memoflow.data.local.entity.GratitudeEntity
import com.example.memoflow.data.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class GratitudeViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _gratitudes = MutableStateFlow<List<GratitudeEntity>>(emptyList())
    val gratitudes: StateFlow<List<GratitudeEntity>> = _gratitudes.asStateFlow()

    private val _dailyCount = MutableStateFlow(0)
    val dailyCount: StateFlow<Int> = _dailyCount.asStateFlow()

    init {
        observeGratitudes()
        updateDailyCount()
    }

    private fun observeGratitudes() {
        viewModelScope.launch {
            repository.allGratitudes.collectLatest { 
                _gratitudes.value = it
            }
        }
    }

    fun updateDailyCount() {
        viewModelScope.launch {
            _dailyCount.value = repository.getGratitudeCountForToday()
        }
    }

    fun addGratitude(text: String, colorHex: String) {
        viewModelScope.launch {
            val now = LocalDate.now()
            val gratitude = GratitudeEntity(
                text = text,
                colorHex = colorHex,
                year = now.year
            )
            repository.insertGratitude(gratitude)
            updateDailyCount()
        }
    }

    fun updateGratitude(gratitude: GratitudeEntity) {
        viewModelScope.launch {
            repository.insertGratitude(gratitude)
        }
    }

    fun deleteGratitude(gratitude: GratitudeEntity) {
        viewModelScope.launch {
            repository.deleteGratitude(gratitude)
            updateDailyCount()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.memoflow.MemoApplication
                return GratitudeViewModel(application.repository) as T
            }
        }
    }
}
