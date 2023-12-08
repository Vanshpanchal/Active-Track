package com.example.fitnessapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Dao
import com.example.fitnessapp.databinding.ActivityHistoryBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private var bindHistory: ActivityHistoryBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindHistory = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(bindHistory?.root)

        setSupportActionBar(bindHistory?.actionbar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = (getString(R.string.exercise_records))

        }

        bindHistory?.actionbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val dao = (application as App).db.historyDao()
        bindHistory?.clearAll?.setOnClickListener {
//            clearAllData(dao)
            runOnUiThread {
                clearAllData(dao)
            }
        }


        getRoomData(dao)


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

}