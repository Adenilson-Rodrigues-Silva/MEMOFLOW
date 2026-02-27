package com.example.memoflow.data.local.dao

import androidx.room.*
import com.example.memoflow.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM user_settings WHERE id = 0")
    fun getUserSettings(): Flow<UserEntity?>

    @Query("SELECT pin FROM user_settings WHERE id = 0")
    suspend fun getUserPin(): String?
}
