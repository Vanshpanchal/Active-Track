package com.example.fitnessapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history-tb")
data class HistoryEntity(
    @PrimaryKey
    val date: String
)
