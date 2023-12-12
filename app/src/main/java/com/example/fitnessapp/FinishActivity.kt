package com.example.fitnessapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.fitnessapp.databinding.ActivityFinishBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.E

class FinishActivity : AppCompatActivity() {
    private var bindFinish: ActivityFinishBinding? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var fsStore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindFinish = ActivityFinishBinding.inflate(layoutInflater)
        setContentView(bindFinish?.root)
        sharedPreferences = applicationContext.getSharedPreferences("ExercisePref", MODE_PRIVATE)
        uploadToStore()
        auth = FirebaseAuth.getInstance()
        fsStore = FirebaseFirestore.getInstance()
        setSupportActionBar(bindFinish?.actionbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        bindFinish?.btnFinish?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)
            finish()

        }

        val dao = (application as App).db.historyDao()
        addDateToDatabase(dao)
    }

    private fun addDateToDatabase(historyDao: HistoryDao) {

        val c = Calendar.getInstance()
        val dateTime = c.time
        Log.e("Date : ", "" + dateTime)

        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        val date = sdf.format(dateTime)
        Log.e("Formatted Date : ", "" + date)

        lifecycleScope.launch {
            historyDao.insert(HistoryEntity(date))
            Log.e(
                "Date : ",
                "Added..."
            )
        }
    }

    private fun uploadToStore() {
        val startDuration = sharedPreferences.getString("Start_time", "")
        val c = Calendar.getInstance()
        val dateTime = c.time
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        val endDuration = sdf.format(dateTime)
        val duration = sharedPreferences.getLong("Duration", 0)
        Log.d("hello", "onCreate: $startDuration $endDuration")

        val entry = hashMapOf(
            "Date" to Timestamp.now(),
            "StartDuration" to startDuration,
            "EndDuration" to endDuration,
            "ExerciseDuration" to duration.toString()
        )
        fsStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        fsStore.collection("Activity").document(auth.currentUser?.uid!!).collection("myActivity")
            .document().set(entry).addOnSuccessListener {
                Log.d("hello", "uploadToStore: Done")
            }
    }

}