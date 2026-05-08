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

    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllNotes(userId: String)

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY date DESC")
    fun getAllNotes(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getNotesInDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET isLocked = 0 WHERE userId = :userId")
    suspend fun unlockAllNotes(userId: String)

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isLocked = 1")
    suspend fun getLockedNotesCount(userId: String): Int

    @Query("SELECT * FROM notes WHERE userId = :userId AND isLocked = 0 AND isTimeCapsule = 0 ORDER BY date ASC")
    fun getRecallableNotes(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND latitude IS NOT NULL AND longitude IS NOT NULL")
    fun getNotesWithLocation(userId: String): Flow<List<NoteEntity>>

    // ✅ Nova query filtrada por data para o Mapa
    @Query("""
        SELECT * FROM notes 
        WHERE userId = :userId
        AND latitude IS NOT NULL 
        AND longitude IS NOT NULL 
        AND date >= :sinceDate
        ORDER BY date DESC
    """)
    fun getNotesWithLocationSince(userId: String, sinceDate: Long): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE userId = :userId
        AND latitude BETWEEN :minLat AND :maxLat 
        AND longitude BETWEEN :minLon AND :maxLon
        AND date >= :sinceDate
        ORDER BY date DESC
    """)
    fun getNotesByLocationAreaFiltered(userId: String, minLat: Double, maxLat: Double, minLon: Double, maxLon: Double, sinceDate: Long): Flow<List<NoteEntity>>

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND date BETWEEN :startOfDay AND :endOfDay")
    suspend fun getCountForDay(userId: String, startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isTimeCapsule = 1")
    suspend fun getTimeCapsuleCount(userId: String): Int
}

