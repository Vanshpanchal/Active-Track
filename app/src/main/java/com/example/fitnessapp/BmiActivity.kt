package com.example.fitnessapp

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnScrollChangeListener
import android.widget.RadioButton
import com.example.fitnessapp.databinding.ActivityBmiBinding
import com.google.android.material.snackbar.Snackbar
import com.google.common.cache.Weigher
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.checkerframework.checker.units.qual.Current
import org.checkerframework.checker.units.qual.Time
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class BmiActivity : AppCompatActivity() {
    private var bindBmi: ActivityBmiBinding? = null
    private var height: Float = 0.0f
    private val inchValue = 12
    private var weight: Float = 0.0f
    private var feet: Int = 0
    private var inch: Int = 0
    private var pound: Float = 0.0f
    private var activeRadio = "Metric"
    private var bmiValue: String = ""
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindBmi = ActivityBmiBinding.inflate(layoutInflater)
        setContentView(bindBmi?.root)

        setSupportActionBar(bindBmi?.actionbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = (getString(R.string.bmi_fullform))

        }
        bindBmi?.actionbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        bindBmi?.scrollView?.setOnScrollChangeListener(object : OnScrollChangeListener {
            override fun onScrollChange(
                v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int
            ) {
                bindBmi?.linearlayout2?.setPadding(0, 0, 0, 20)
            }

        })


        bindBmi?.radioGrp?.setOnCheckedChangeListener { _, id ->
            bindBmi?.linearlayout1?.visibility = View.GONE
            bindBmi?.linearlayout2?.visibility = View.GONE

            bindBmi?.check?.visibility = View.GONE
            bindBmi?.tvBmi?.visibility = View.GONE
            val opt = findViewById<RadioButton>(id)
            when (opt.tag) {
                "US" -> {
                    bindBmi?.textInputLayout1?.visibility = View.GONE
                    bindBmi?.textInputLayout3?.visibility = View.VISIBLE
                    bindBmi?.textInputLayout2?.visibility = View.GONE
                    bindBmi?.textInputLayout4?.visibility = View.VISIBLE
                    activeRadio = "US"

                }

                "metric" -> {
                    activeRadio = "Metric"
                    bindBmi?.textInputLayout1?.visibility = View.VISIBLE
                    bindBmi?.textInputLayout3?.visibility = View.GONE
                    bindBmi?.textInputLayout4?.visibility = View.GONE
                    bindBmi?.textInputLayout2?.visibility = View.VISIBLE

                }

                else -> {
                    Log.d("radioBtn", "ERROR")
                }
            }
        }

        bindBmi?.calculate?.setOnClickListener {
            if (activeRadio == "Metric") {
                if (bindBmi?.height?.text.isNullOrBlank() && bindBmi?.weight?.text.isNullOrBlank()) {
                    val bar =
                        Snackbar.make(
                            bindBmi!!.root,
                            "Field should not be empty",
                            Snackbar.LENGTH_SHORT
                        )
                    bar.setBackgroundTint(Color.parseColor("#001780"))
                    bar.setAction("OK", View.OnClickListener {
                        bar.dismiss()
                    })
                    bar.setActionTextColor(Color.WHITE)
                    bar.show()
                } else {
                    calculateMetricBmi()

                }
            } else if (activeRadio == "US") {
                if (bindBmi?.weightPounds?.text.isNullOrBlank()) {
                    val bar =
                        Snackbar.make(
                            bindBmi!!.root,
                            "Field should not be empty",
                            Snackbar.LENGTH_SHORT
                        )
                    bar.setBackgroundTint(Color.parseColor("#001780"))
                    bar.setAction("OK", View.OnClickListener {
                        bar.dismiss()
                    })
                    bar.setActionTextColor(Color.WHITE)
                    bar.show()
                } else {
                    calculateUsBmi()
                }
            }

        }
    }

    private fun calculateUsBmi() {
        if (bindBmi?.inch?.text.toString().isNotBlank() && bindBmi?.feet?.text.toString()
                .isNotBlank()
        ) {


            if (bindBmi?.inch?.text.toString().toInt() <= 12) {
                feet = bindBmi?.feet?.text.toString().toInt()
                inch = bindBmi?.inch?.text.toString().toInt()
                pound = bindBmi?.weightPounds?.text.toString().toFloat()

                val totalInch = (feet * inchValue) + inch
                val bmi = (pound / (totalInch * totalInch)) * 703
                val shortenBmi =
                    BigDecimal(bmi.toDouble()).setScale(1, RoundingMode.HALF_EVEN).toString()
                bindBmi?.tvBmi?.text = "Your BMI is $shortenBmi"
                bindBmi?.linearlayout1?.visibility = View.VISIBLE
                bindBmi?.linearlayout2?.visibility = View.VISIBLE
                bindBmi?.check?.visibility = View.VISIBLE

                bindBmi?.tvBmi?.visibility = View.VISIBLE



                bmiValue = shortenBmi
                val heightval = "$feet'$inch"
                Log.d("hello", "calculateUsBmi: ${heightval}")
                val weightKg =
                    BigDecimal((pound * 0.453592)).setScale(1, RoundingMode.HALF_EVEN).toString()
                uploadData(heightval, weightKg)
                storingPref(bmiValue, heightval, weightKg)

                bindBmi?.height?.text?.clear()
                bindBmi?.height?.clearFocus()
                bindBmi?.weightPounds?.clearFocus()
                bindBmi?.weightPounds?.text?.clear()
                bindBmi?.inch?.clearFocus()
                bindBmi?.inch?.text?.clear()
                bindBmi?.feet?.clearFocus()
                bindBmi?.feet?.text?.clear()

            } else {
                val bar =
                    Snackbar.make(
                        bindBmi!!.root,
                        "Inch value should be less than 12",
                        Snackbar.LENGTH_SHORT
                    )
                bar.setBackgroundTint(Color.parseColor("#001780"))
                bar.setAction("OK", View.OnClickListener {
                    bar.dismiss()
                })
                bar.setActionTextColor(Color.WHITE)
                bar.show()
            }
        } else {
            val bar =
                Snackbar.make(bindBmi!!.root, "Field should not be empty", Snackbar.LENGTH_SHORT)
            bar.setBackgroundTint(Color.parseColor("#001780"))
            bar.setAction("OK", View.OnClickListener {
                bar.dismiss()
            })
            bar.setActionTextColor(Color.WHITE)
            bar.show()
        }


    }

    private fun calculateMetricBmi() {
        height = bindBmi?.height?.text.toString().toFloat() / 100
        weight = bindBmi?.weight?.text.toString().toFloat()

        val bmi = (weight / (height * height))
        val shortenBmi =
            BigDecimal(bmi.toDouble()).setScale(1, RoundingMode.HALF_EVEN).toString()
        bindBmi?.tvBmi?.text = "Your BMI is $shortenBmi"
        bindBmi?.linearlayout1?.visibility = View.VISIBLE
        bindBmi?.linearlayout2?.visibility = View.VISIBLE
        bindBmi?.check?.visibility = View.VISIBLE
        bindBmi?.tvBmi?.visibility = View.VISIBLE

        bmiValue = shortenBmi
        uploadData(bindBmi?.height?.text.toString(), bindBmi?.weight?.text.toString())
        storingPref(bmiValue, bindBmi?.height?.text.toString(), bindBmi?.weight?.text.toString())
        bindBmi?.height?.text?.clear()
        bindBmi?.height?.clearFocus()
        bindBmi?.weight?.clearFocus()
        bindBmi?.weight?.text?.clear()
    }

    private fun uploadData(heightval: String, weightval: String) {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val current = LocalDateTime.now().format(formatter)
        val entry = hashMapOf(
            "bmi" to bmiValue,
            "time" to current,
            "timestamp" to Timestamp.now(),
            "height" to heightval,
            "weight" to weightval
        )

        firestore.collection("BMI").document(auth.currentUser?.uid!!).collection("MyBMI").document()
            .set(entry).addOnSuccessListener {
                Log.d("hello", "uploadData: Done")
            }
    }

    private fun storingPref(Bmi: String, height: String, weight: String) {
        Log.d("hello", "storingPref: $height $weight")
        sharedPreferences = getSharedPreferences("HEALTH-DATA", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.clear()
        editor.putString("Bmi", Bmi)
        editor.putString("height", height)
        editor.putString("weight", weight)
        editor.commit()
    }
}