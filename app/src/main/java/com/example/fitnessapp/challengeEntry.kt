package com.example.fitnessapp

import com.google.firebase.Timestamp

data class challengeEntry(

    val Diet : String ? = null,
    val Workout : String ? = null,
    val Water : String ? = null,
    val Reading : String ? = null,
    val ProgressPic : String ? = null,
    val Learning :String ? = null,
    val Streak : String ? = null,
    val Date : String ? = null,
    val Time : Timestamp ? = null,
)
