package com.example.waterme.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plant ORDER BY id ASC")
    fun getAll(): Flow<List<Plant>>

    @Query("UPDATE Plant SET is_active = NOT is_active WHERE id == :id")
    fun toggleActive(id: Long)
}