package com.example.fitnessapp

import com.google.firebase.Timestamp
import java.util.Date

data class bmiEntry(
    var bmi: String? = null,
    var time: String? = null,
    var timestamp: Timestamp? = null,
    var height: String? = null,
    var weight: String? = null
)
