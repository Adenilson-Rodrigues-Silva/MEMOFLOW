package com.example.memoflow.data.repository

import com.example.memoflow.data.local.dao.GoalDao
import com.example.memoflow.data.local.dao.NoteDao
import com.example.memoflow.data.local.dao.UserDao
import com.example.memoflow.data.local.entity.GoalEntity
import com.example.memoflow.data.local.entity.NoteEntity
import com.example.memoflow.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class MemoRepository(
    private val noteDao: NoteDao,
    private val userDao: UserDao,
    private val goalDao: GoalDao
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

    // Goals
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    val activeGoals: Flow<List<GoalEntity>> = goalDao.getActiveGoals()
    val completedGoals: Flow<List<GoalEntity>> = goalDao.getCompletedGoals()

    suspend fun insertGoal(goal: GoalEntity) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = goalDao.deleteGoal(goal)
    suspend fun getGoalById(id: Long) = goalDao.getGoalById(id)
}
