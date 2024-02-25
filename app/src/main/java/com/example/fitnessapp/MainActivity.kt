package com.example.fitnessapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import com.example.fitnessapp.databinding.ActivityMainBinding
import com.google.firebase.Timestamp
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var bindMain: ActivityMainBinding? = null
    private var exercisetimer: Long = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    private lateinit var sharedPreferences_1: SharedPreferences
    private lateinit var editor_1: Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindMain?.root)

        sharedPreferences = getSharedPreferences("ExercisePref", MODE_PRIVATE)
        editor = sharedPreferences.edit()



        bindMain?.flStart?.setOnClickListener {
            val c = Calendar.getInstance()
            val dateTime = c.time
            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
            val current = sdf.format(dateTime)
            val intent = Intent(this, ExerciseActivity::class.java)
            editor.clear()
            editor.putLong("Duration", exercisetimer)
            editor.putString("Start_time", current.toString())
            editor.commit()
            startActivity(intent)
        }

        bindMain?.btnBmi?.setOnClickListener {
            val intent = Intent(this, BmiActivity::class.java)
            startActivity(intent)
        }

        bindMain?.btnHistory?.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        bindMain?.radioGroup?.setOnCheckedChangeListener { _, id ->

            val view = findViewById<RadioButton>(id)
            exercisetimer = when (view.tag) {
                "30" -> 30
                "40" -> 40
                "50" -> 50
                else -> {
                    30
                }
            }
        }

        sharedPreferences_1 = applicationContext.getSharedPreferences("selectAct", MODE_PRIVATE)
        editor_1 = sharedPreferences.edit()
        val value = sharedPreferences_1.getString("act", "0")
        if (value == "workout") {
            bindMain!!.radioGroup.visibility = View.VISIBLE
            bindMain!!.ivLogo.setImageResource(R.drawable.home_workout_written)
            exercisetimer = 30
        }else{
            bindMain!!.radioGroup.visibility = View.GONE
            exercisetimer = 20
            bindMain!!.ivLogo.setImageResource(R.drawable.home_yoga_written)
        }

    }
}