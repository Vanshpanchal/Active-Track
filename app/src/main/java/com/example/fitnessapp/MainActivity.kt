package com.example.fitnessapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import com.example.fitnessapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var bindMain: ActivityMainBinding? = null
    private var exercisetimer: Long = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindMain?.root)

        sharedPreferences = getSharedPreferences("Main", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        bindMain?.flStart?.setOnClickListener {
            val intent = Intent(this, ExerciseActivity::class.java)
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
            editor.clear()
            editor.putLong("Duration", exercisetimer)
            editor.commit()
        }


    }
}