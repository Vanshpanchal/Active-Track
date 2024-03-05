package com.example.fitnessapp

data class quotemydata(
    val limit: Int,
    val quotes: List<Quote>,
    val skip: Int,
    val total: Int
)