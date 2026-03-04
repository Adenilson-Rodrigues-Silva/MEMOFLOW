package com.example.memoflow.data.repository

import com.example.memoflow.data.local.dao.GratitudeDao
import com.example.memoflow.data.local.dao.NoteDao
import com.example.memoflow.data.local.dao.UserDao
import com.example.memoflow.data.local.entity.GratitudeEntity
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class MemoRepository(
    private val noteDao: NoteDao,
    private val userDao: UserDao,
    private val gratitudeDao: GratitudeDao
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

    fun getRecallableNotes(): Flow<List<NoteEntity>> = noteDao.getRecallableNotes()

    // User Settings
    val userSettings: Flow<UserEntity?> = userDao.getUserSettings()
    suspend fun saveUserSettings(user: UserEntity) = userDao.insertUser(user)
    suspend fun getUserPin() = userDao.getUserPin()

    // Gratitude
    val allGratitudes: Flow<List<GratitudeEntity>> = gratitudeDao.getAllGratitudes()
    
    fun getGratitudesByYear(year: Int) = gratitudeDao.getGratitudesByYear(year)

    suspend fun insertGratitude(gratitude: GratitudeEntity) = gratitudeDao.insertGratitude(gratitude)
    
    suspend fun deleteGratitude(gratitude: GratitudeEntity) = gratitudeDao.deleteGratitude(gratitude)

    suspend fun getGratitudeCountForToday(): Int {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return gratitudeDao.getCountForDay(startOfDay, endOfDay)
    }
}
