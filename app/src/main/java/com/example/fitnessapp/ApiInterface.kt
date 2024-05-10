package com.example.fitnessapp

import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("quotes")
    fun getQuote(): Call<quotemydata>
}