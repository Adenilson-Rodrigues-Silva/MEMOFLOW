package com.example.memoflow.ui.viewmodel

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

sealed class DateMark {
    object Normal : DateMark()
    object Locked : DateMark()
    object Capsule : DateMark()
}

class HomeViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notes: StateFlow<List<NoteEntity>> = _notes.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _markedDates = MutableStateFlow<Map<LocalDate, DateMark>>(emptyMap())
    val markedDates: StateFlow<Map<LocalDate, DateMark>> = _markedDates.asStateFlow()

    private val _allNotesList = mutableListOf<NoteEntity>()

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            repository.allNotes.collectLatest { allNotes ->
                _allNotesList.clear()
                _allNotesList.addAll(allNotes)
                updateMarkedDates(allNotes)
                filterNotesByDate(allNotes, _selectedDate.value)
            }
        }
    }

    private fun updateMarkedDates(allNotes: List<NoteEntity>) {
        val marks = mutableMapOf<LocalDate, DateMark>()
        allNotes.forEach { note ->
            val date = Instant.ofEpochMilli(note.date).atZone(ZoneId.systemDefault()).toLocalDate()
            // Prioridade: Cápsula > Trancada > Normal
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
        filterNotesByDate(_allNotesList, date)
    }

    private fun filterNotesByDate(allNotes: List<NoteEntity>, date: LocalDate) {
        val filtered = allNotes.filter { note ->
            val noteDate = Instant.ofEpochMilli(note.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            noteDate == date
        }
        _notes.value = filtered
    }

    fun canAddNote(): Boolean {
        return _notes.value.size < 3
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
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.memoflow.MemoApplication
                return HomeViewModel(application.repository) as T
            }
        }
    }
}
