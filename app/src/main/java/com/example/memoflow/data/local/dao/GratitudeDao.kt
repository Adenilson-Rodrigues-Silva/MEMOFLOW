package com.example.memoflow.data.local.dao

import androidx.room.*
import com.example.memoflow.data.local.entity.GratitudeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GratitudeDao {
    @Query("SELECT * FROM gratitudes ORDER BY date DESC")
    fun getAllGratitudes(): Flow<List<GratitudeEntity>>

    @Query("SELECT * FROM gratitudes WHERE year = :year ORDER BY date DESC")
    fun getGratitudesByYear(year: Int): Flow<List<GratitudeEntity>>

    @Query("SELECT COUNT(*) FROM gratitudes WHERE date >= :startOfDay AND date <= :endOfDay")
    suspend fun getCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGratitude(gratitude: GratitudeEntity)

    @Delete
    suspend fun deleteGratitude(gratitude: GratitudeEntity)
}
