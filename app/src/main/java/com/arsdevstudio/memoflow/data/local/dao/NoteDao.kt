package com.arsdevstudio.memoflow.data.local.dao

import androidx.room.*
import com.arsdevstudio.memoflow.data.local.entity.NoteEntity
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

    @Query("SELECT * FROM notes WHERE latitude IS NOT NULL AND longitude IS NOT NULL")
    fun getNotesWithLocation(): Flow<List<NoteEntity>>

    // ✅ Nova query filtrada por data para o Mapa
    @Query("""
        SELECT * FROM notes 
        WHERE latitude IS NOT NULL 
        AND longitude IS NOT NULL 
        AND date >= :sinceDate
        ORDER BY date DESC
    """)
    fun getNotesWithLocationSince(sinceDate: Long): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE latitude BETWEEN :minLat AND :maxLat 
        AND longitude BETWEEN :minLon AND :maxLon
        AND date >= :sinceDate
        ORDER BY date DESC
    """)
    fun getNotesByLocationAreaFiltered(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double, sinceDate: Long): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM notes WHERE date BETWEEN :startOfDay AND :endOfDay")
    suspend fun getCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM notes WHERE isTimeCapsule = 1")
    suspend fun getTimeCapsuleCount(): Int
}

