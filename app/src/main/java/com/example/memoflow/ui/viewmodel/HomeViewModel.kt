package com.example.memoflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    // Estado para o texto do diário
    private val _diaryText = MutableStateFlow("")
    val diaryText: StateFlow<String> = _diaryText.asStateFlow()

    // Estado para saber se o menu está aberto
    private val _isMenuExpanded = MutableStateFlow(false)
    val isMenuExpanded: StateFlow<Boolean> = _isMenuExpanded.asStateFlow()

    fun toggleMenu() {
        _isMenuExpanded.value = !_isMenuExpanded.value
    }

    fun updateDiaryText(newText: String) {
        _diaryText.value = newText
    }
}