package com.arsdevstudio.memoflow.data.local.dao

import androidx.room.*
import com.arsdevstudio.memoflow.data.local.entity.GratitudeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GratitudeDao {
    @Query("SELECT * FROM gratitudes WHERE userId = :userId ORDER BY date DESC")
    fun getAllGratitudes(userId: String): Flow<List<GratitudeEntity>>

    @Query("SELECT * FROM gratitudes WHERE userId = :userId AND year = :year ORDER BY date DESC")
    fun getGratitudesByYear(userId: String, year: Int): Flow<List<GratitudeEntity>>

    @Query("SELECT COUNT(*) FROM gratitudes WHERE userId = :userId AND date >= :startOfDay AND date <= :endOfDay")
    suspend fun getCountForDay(userId: String, startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT * FROM gratitudes WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getGratitudesInDateRange(userId: String, startDate: Long, endDate: Long): kotlinx.coroutines.flow.Flow<List<GratitudeEntity>>

    @Query("SELECT COUNT(*) FROM gratitudes WHERE userId = :userId")
    suspend fun getTotalCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGratitude(gratitude: GratitudeEntity)

    @Delete
    suspend fun deleteGratitude(gratitude: GratitudeEntity)

    @Query("DELETE FROM gratitudes WHERE userId = :userId")
    suspend fun deleteAllGratitudes(userId: String)
}

