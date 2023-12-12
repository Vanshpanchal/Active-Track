package com.example.fitnessapp

import com.google.firebase.Timestamp

data class profileEntry(
    var bmi: String? = null,
    var time: String? = null,
    var timestamp: Timestamp? = null,
    var height: String? = null,
    var weight: String? = null
)