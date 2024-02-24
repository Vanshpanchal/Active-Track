package com.example.fitnessapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessapp.databinding.ActivityExerciseBinding
import com.example.fitnessapp.databinding.ConfirmationDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var restProgress = 0
    private var exerciseProgress = 0
    private var restDuration: Long = 1 // Actual It was 10 , for debug it is change to 1
    private var exerciseDuration: Long = 1
    private var exerciseTimerDuration: Int = 1
    private var restTimer: CountDownTimer? = null
    private var exerciseTimer: CountDownTimer? = null
    private var bindExercise: ActivityExerciseBinding? = null
    private var exerciseCurrentPosition = -1
    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var textToSpeech: TextToSpeech? = null
    private var player: MediaPlayer? = null
    private var exerciseAdapter: ExerciseAdapter? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    private lateinit var sharedPreferences_1: SharedPreferences
    private lateinit var editor_1: Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindExercise = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(bindExercise?.root)

        setSupportActionBar(bindExercise?.actionbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }


        sharedPreferences = applicationContext.getSharedPreferences("ExercisePref", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        var duration = sharedPreferences.getLong("Duration", 30)
        val startDuration = sharedPreferences.getString("Start_time", "")
        Log.d("hello", "___onCreate: $duration $startDuration")
        // debug
        exerciseDuration = 1
        exerciseTimerDuration = 1


        // Actual Initialization
//        exerciseDuration = duration
//        exerciseTimerDuration = duration.toInt()


        sharedPreferences_1 = applicationContext.getSharedPreferences("selectAct", MODE_PRIVATE)
        editor_1 = sharedPreferences.edit()

        val value = sharedPreferences_1.getString("act", "0")
        Log.d("act_check", "onCreate: $value")

        if (value == "workout") {
            exerciseList = Constants.ExerciseList()

        } else {
            exerciseList = Constantone.ExerciseList()

        }

        bindExercise?.actionbar?.setNavigationOnClickListener {
            customBackBtn()
        }

        textToSpeech = TextToSpeech(this, this)


        settingUpRestView()
        settingUpRecyclerView()
    }

    private fun settingUpRestView() {
        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }
        try {
            player = MediaPlayer.create(this, R.raw.sound)
            player?.isLooping = false
            player?.start()
        } catch (e: Exception) {
            Log.d("MediaPlayer", "settingUpRestView: ${e.stackTrace}")
        }
        restTimer()
    }

    private fun settingUpExerciseView() {
        bindExercise?.ivExercise?.visibility = View.VISIBLE
        if (exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        bindExercise?.tvStatement?.text =
            exerciseList?.get(exerciseCurrentPosition)?.getName().toString()

        bindExercise?.ivExercise?.setImageResource(
            exerciseList?.get(exerciseCurrentPosition)!!.getImage()
        )
        speakExerciseName(exerciseList?.get(exerciseCurrentPosition)?.getName().toString())
        bindExercise?.progressTimer?.progress = exerciseTimerDuration
        bindExercise?.progressTimer?.max = exerciseTimerDuration
        exerciseTimer()
    }

    private fun restTimer() {
        speakExerciseName("Relax and take a deep breathe and get ready for next exercise")
        bindExercise?.progressTimer?.progress = 0
        bindExercise?.progressTimer?.progress = 100
        bindExercise?.progressTimer?.max = 10
        bindExercise?.ivExercise?.setImageResource(R.drawable.relax)
        restTimer = object : CountDownTimer(restDuration * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                val nextExercise =
                    getString(R.string.get_ready_for) + "\n" + exerciseList?.get(
                        exerciseCurrentPosition + 1
                    )
                        ?.getName()
                bindExercise?.tvStatement?.text = nextExercise
                bindExercise?.progressTimer?.progress = 10 - restProgress
                bindExercise?.tvTimer?.text = (10 - restProgress).toString()
            }


            @SuppressLint("NotifyDataSetChanged")
            override fun onFinish() {

//                Toast.makeText(this@ExerciseActivity, "Done", Toast.LENGTH_SHORT).show()
                exerciseCurrentPosition++
                exerciseList!![exerciseCurrentPosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()
                settingUpExerciseView()
            }

        }.start()
    }

    private fun exerciseTimer() {

        exerciseTimer = object : CountDownTimer(exerciseDuration * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                bindExercise?.progressTimer?.progress = exerciseTimerDuration - exerciseProgress
                bindExercise?.tvTimer?.text = (exerciseTimerDuration - exerciseProgress).toString()
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onFinish() {
//                Toast.makeText(this@ExerciseActivity, "Exercise Done", Toast.LENGTH_SHORT).show()
                exerciseList?.get(exerciseCurrentPosition)?.setIsCompleted(true)
                exerciseList?.get(exerciseCurrentPosition)?.setIsSelected(false)
                exerciseAdapter!!.notifyDataSetChanged()
                if (exerciseCurrentPosition < exerciseList?.size!! - 1) {
                    settingUpRestView()
                } else {
                    val intent = Intent(this@ExerciseActivity, FinishActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        }.start()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d("TTS", "onInit: Language Not Supported")
            }
        } else {
            Log.d("TTS", "ERROR")
        }
    }

    private fun speakExerciseName(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun settingUpRecyclerView() {

        bindExercise?.rvStatus?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        exerciseAdapter = ExerciseAdapter(exerciseList!!)
        bindExercise?.rvStatus?.adapter = exerciseAdapter
    }

    private fun customBackBtn() {


//        val dialog = Dialog(this)
//        val layout = ConfirmationDialogBinding.inflate(layoutInflater)
//        dialog.setContentView(layout.root)
//        dialog.setCanceledOnTouchOutside(false)
//
//        layout.no.setOnClickListener {
//            dialog.dismiss()
//            Log.d("Btn", "No Clicked")
//        }
//        layout.yes.setOnClickListener {
//            this@ExerciseActivity.finish()
//            Log.d("Btn", "Yes Clicked")
//            dialog.dismiss()
//        }
//
//        dialog.show()

        MaterialAlertDialogBuilder(
            this@ExerciseActivity,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setTitle("Quit?")
            .setIcon(R.drawable.dialog_logo)
            .setMessage("Are You sure you want to quit?")
            .setPositiveButton("Yes") { dialog, which ->
                this@ExerciseActivity.finish()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show();
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        customBackBtn()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

        if (exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        if (textToSpeech != null) {
            textToSpeech?.stop()
            textToSpeech?.shutdown()

        }
        if (player != null) {
            player?.stop()
        }
        bindExercise = null
    }
}