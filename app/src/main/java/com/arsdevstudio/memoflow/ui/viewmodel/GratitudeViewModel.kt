package com.arsdevstudio.memoflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.arsdevstudio.memoflow.MemoApplication
import com.arsdevstudio.memoflow.data.local.entity.GratitudeEntity
import com.arsdevstudio.memoflow.data.local.entity.UserEntity
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import com.arsdevstudio.memoflow.utils.BillingPrefs
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class GratitudeViewModel(
    application: Application,
    private val repository: MemoRepository,
    private val billingPrefs: BillingPrefs
) : AndroidViewModel(application) {

    private val _allGratitudes = MutableStateFlow<List<GratitudeEntity>>(emptyList())
    val allGratitudes: StateFlow<List<GratitudeEntity>> = _allGratitudes.asStateFlow()

    val todaysGratitudes: StateFlow<List<GratitudeEntity>> = _allGratitudes.map { list ->
        val today = LocalDate.now()
        list.filter {
            val date = java.time.Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
            date == today
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dailyCount = MutableStateFlow(0)
    val dailyCount: StateFlow<Int> = _dailyCount.asStateFlow()

    private val _flashbackCount = MutableStateFlow(0)
    val flashbackCount: StateFlow<Int> = _flashbackCount.asStateFlow()
    
    private val _maxFlashbacks = MutableStateFlow(3)
    val maxFlashbacks: StateFlow<Int> = _maxFlashbacks.asStateFlow()

    init {
        observeGratitudes()
        updateDailyCount()
        checkFlashbackLimit()
        observePremiumStatus()
    }

    private fun observeGratitudes() {
        viewModelScope.launch {
            repository.allGratitudes.collectLatest { 
                _allGratitudes.value = it
            }
        }
    }

    private fun observePremiumStatus() {
        viewModelScope.launch {
            billingPrefs.isPremium.collectLatest { isPremium ->
                _maxFlashbacks.value = if (isPremium) 6 else 3
            }
        }
    }

    fun updateDailyCount() {
        viewModelScope.launch {
            _dailyCount.value = repository.getGratitudeCountForToday()
        }
    }

    private fun checkFlashbackLimit() {
        viewModelScope.launch {
            val user = repository.userSettings.first() ?: UserEntity()
            val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            if (user.lastGratitudeRecallDate < today) {
                // Novo dia, reseta contador no DB
                repository.saveUserSettings(user.copy(
                    lastGratitudeRecallDate = System.currentTimeMillis(),
                    gratitudeRecallCount = 0
                ))
                _flashbackCount.value = 0
            } else {
                _flashbackCount.value = user.gratitudeRecallCount
            }
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

    fun incrementFlashbackCount() {
        viewModelScope.launch {
            val user = repository.userSettings.first() ?: UserEntity()
            val isPremium = billingPrefs.isPremium.first()
            val limit = if (isPremium) 6 else 3
            
            if (user.gratitudeRecallCount < limit) {
                val newCount = user.gratitudeRecallCount + 1
                repository.saveUserSettings(user.copy(gratitudeRecallCount = newCount))
                _flashbackCount.value = newCount
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as MemoApplication
                return GratitudeViewModel(application, application.repository, application.billingPrefs) as T
            }
        }
    }
}

