package com.example.memoflow.data.local

import androidx.room.TypeConverter
import com.example.memoflow.data.local.entity.GoalStep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromGoalStepList(value: List<GoalStep>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toGoalStepList(value: String): List<GoalStep> {
        val listType = object : TypeToken<List<GoalStep>>() {}.type
        return gson.fromJson(value, listType)
    }
}
