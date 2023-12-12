package com.example.fitnessapp

import com.google.firebase.Timestamp

data class ActivtyEntry(
    val Date: Timestamp? = null,
    val StartDuration: String? = null,
    val EndDuration: String? = null,
    val ExerciseDuration: String? = null
)
