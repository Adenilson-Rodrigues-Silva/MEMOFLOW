package com.arsdevstudio.memoflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
import com.arsdevstudio.memoflow.data.repository.MemoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant

sealed class DateMark {
    object Normal : DateMark()
    object Locked : DateMark()
    object Capsule : DateMark()
}

class HomeViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _allNotesList = MutableStateFlow<List<NoteEntity>>(emptyList())
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _markedDates = MutableStateFlow<Map<LocalDate, DateMark>>(emptyMap())
    val markedDates: StateFlow<Map<LocalDate, DateMark>> = _markedDates.asStateFlow()

    // Volta para a lógica original: apenas notas do dia selecionado
    val notes: StateFlow<List<NoteEntity>> = combine(_allNotesList, _selectedDate) { allNotes, date ->
        allNotes.filter { note ->
            val noteDate = Instant.ofEpochMilli(note.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            noteDate == date
        }.sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            repository.allNotes.collectLatest { allNotes ->
                _allNotesList.value = allNotes
                updateMarkedDates(allNotes)
            }
        }
    }

    private fun updateMarkedDates(allNotes: List<NoteEntity>) {
        val marks = mutableMapOf<LocalDate, DateMark>()
        allNotes.forEach { note ->
            val date = Instant.ofEpochMilli(note.date).atZone(ZoneId.systemDefault()).toLocalDate()
            val currentMark = marks[date]
            if (note.isTimeCapsule) {
                marks[date] = DateMark.Capsule
            } else if (note.isLocked && currentMark != DateMark.Capsule) {
                marks[date] = DateMark.Locked
            } else if (currentMark == null) {
                marks[date] = DateMark.Normal
            }
        }
        _markedDates.value = marks
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun canAddNote(): Boolean {
        val date = _selectedDate.value
        return _allNotesList.value.count { 
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() == date 
        } < 3
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.arsdevstudio.memoflow.MemoApplication
                return HomeViewModel(application.repository) as T
            }
        }
    }
}

