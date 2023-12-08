package com.example.fitnessapp

import android.app.Application

class App: Application() {
    val db:HistoryDatabase by lazy {
        HistoryDatabase.getInstance(this)
    }
}