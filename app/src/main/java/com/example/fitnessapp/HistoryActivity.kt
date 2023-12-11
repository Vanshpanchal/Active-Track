package com.example.fitnessapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Dao
import com.example.fitnessapp.databinding.ActivityHistoryBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private var activeRadio = "EXERCISE"
    private var bindHistory: ActivityHistoryBinding? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var bmiList: ArrayList<bmiEntry>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindHistory = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(bindHistory?.root)

        setSupportActionBar(bindHistory?.actionbar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = (getString(R.string.history))

        }

        bindHistory?.actionbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        bmiList = arrayListOf()

        bindHistory?.rvBmi?.layoutManager =
            LinearLayoutManager(this@HistoryActivity)
        val bmiAdapter = BmiAdapter(bmiList)
        bindHistory!!.rvBmi.adapter = bmiAdapter

        val dao = (application as App).db.historyDao()
        bindHistory?.clearAll?.setOnClickListener {
//            clearAllData(dao)
            if (activeRadio == "EXERCISE") {
                runOnUiThread {
                    clearAllData(dao)
                }
            }
        }
        bindHistory?.radioGrp?.setOnCheckedChangeListener { _, id ->

            val opt = findViewById<RadioButton>(id)
            when (opt.tag) {
                "EXERCISE" -> {
                    bindHistory?.rvRecords?.visibility = View.VISIBLE
                    bindHistory?.rvBmi?.visibility = View.GONE
                    bindHistory?.clearAll?.visibility = View.VISIBLE
                    activeRadio = "EXERCISE"
                    getRoomData(dao)
                }

                "BMI" -> {
                    bindHistory?.rvRecords?.visibility = View.GONE
                    bindHistory?.rvBmi?.visibility = View.VISIBLE
                    bindHistory?.clearAll?.visibility = View.GONE
                    activeRadio = "BMI"
                }

                else -> {
                    Log.d("hello", "ERROR")
                }
            }
        }


        getRoomData(dao)
        getBmiData()


    }

    private fun getRoomData(historyDao: HistoryDao) {
        lifecycleScope.launch {
            historyDao.fetchData().collect() { datalist ->
                if (datalist.isNotEmpty()) {
                    bindHistory?.status?.visibility = View.GONE
                    bindHistory?.rvRecords?.visibility = View.VISIBLE


                    bindHistory?.rvRecords?.layoutManager =
                        LinearLayoutManager(this@HistoryActivity)
//

                    val dateList = ArrayList<String>()
                    for (i in datalist) {
                        dateList.add(i.date)
                    }

                    val HistoryAdapter = HistoryAdapter(dateList)
                    bindHistory?.rvRecords?.adapter = HistoryAdapter
                } else {
                    bindHistory?.status?.visibility = View.VISIBLE
                    bindHistory?.rvRecords?.visibility = View.GONE
                }
            }
        }
    }

    private fun clearAllData(historyDao: HistoryDao) {
        lifecycleScope.launch {
            val size = historyDao.entrySize()
            if (size > 0) {
                historyDao.deleteAllData()
            } else {
                val bar =
                    Snackbar.make(
                        bindHistory!!.root,
                        "No Records Are Available",
                        Snackbar.LENGTH_SHORT
                    )
                bar.setBackgroundTint(Color.parseColor("#001780"))
                bar.setAction("OK", View.OnClickListener {
                    bar.dismiss()
                })
                bar.setActionTextColor(Color.WHITE)
                bar.show()
            }
        }
    }

    private fun getBmiData() {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        firestore.collection("BMI").document(auth.currentUser?.uid!!).collection("MyBMI").orderBy(
            "timestamp",
            Query.Direction.DESCENDING
        ).get()
            .addOnSuccessListener { it ->
                if (!it.isEmpty) {
                    for (data in it) {
                        val obj = data.toObject(bmiEntry::class.java)
                        if (obj != null) {
                            bmiList.add(obj)
                        }
                    }
                    Log.d("hello", "getBmiData: $bmiList")
                    bindHistory?.rvBmi?.adapter = BmiAdapter(bmiList)
                }
            }
    }
}