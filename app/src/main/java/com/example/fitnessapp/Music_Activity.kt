package com.example.fitnessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.fitnessapp.databinding.ActivityMusicBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.random.Random

class Music_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityMusicBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding?.actionbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = (getString(R.string.app_name))

        }
        binding?.actionbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)

        val QuoteData = retrofitBuilder.getQuote()
        QuoteData.enqueue(object : Callback<quotemydata> {
            override fun onResponse(call: Call<quotemydata>, response: Response<quotemydata>) {
                val randomnNumber = Random.nextInt(0, 30)
                Log.d("Api call", "onResponse:  ${randomnNumber}")
                Log.d("Api call", "onResponse:  ${response.body()?.limit}")
                Log.d(
                    "Api call",
                    "onResponse:  ${response.body()?.quotes?.get(randomnNumber)?.quote!!}"
                )

                binding.dailyQuote.text = response.body()?.quotes?.get(randomnNumber)?.quote!!
                binding.quoteCard.visibility = View.VISIBLE

            }

            override fun onFailure(call: Call<quotemydata>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }
}