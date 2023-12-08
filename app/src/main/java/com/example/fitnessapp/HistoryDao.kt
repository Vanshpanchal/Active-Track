package com.example.fitnessapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(historyEntity: HistoryEntity)

    @Query("SELECT * FROM `HISTORY-TB`")
    fun fetchData(): Flow<List<HistoryEntity>>

    @Query("DELETE FROM `HISTORY-TB`")
    suspend fun deleteAllData()

    @Query("SELECT COUNT(*) FROM `HISTORY-TB`")
    suspend fun entrySize(): Int


}