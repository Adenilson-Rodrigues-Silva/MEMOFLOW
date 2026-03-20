package com.example.memoflow.data.local.dao

import androidx.room.*
import com.example.memoflow.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getNotesInDateRange(startDate: Long, endDate: Long): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET isLocked = 0")
    suspend fun unlockAllNotes()

    @Query("SELECT COUNT(*) FROM notes WHERE isLocked = 1")
    suspend fun getLockedNotesCount(): Int

    @Query("SELECT * FROM notes WHERE isLocked = 0 AND isTimeCapsule = 0 ORDER BY date ASC")
    fun getRecallableNotes(): Flow<List<NoteEntity>>
}
