package com.example.memoflow.data.repository

import com.example.memoflow.data.local.dao.NoteDao
import com.example.memoflow.data.local.dao.UserDao
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class MemoRepository(
    private val noteDao: NoteDao,
    private val userDao: UserDao
) {
    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)
    
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    
    suspend fun getNoteById(id: Long) = noteDao.getNoteById(id)

    suspend fun unlockAllNotes() = noteDao.unlockAllNotes()

    fun getNotesInDateRange(startDate: Long, endDate: Long): Flow<List<NoteEntity>> {
        return noteDao.getNotesInDateRange(startDate, endDate)
    }

    // User Settings
    val userSettings: Flow<UserEntity?> = userDao.getUserSettings()
    
    suspend fun saveUserSettings(user: UserEntity) = userDao.insertUser(user)
    
    suspend fun getUserPin() = userDao.getUserPin()
}
