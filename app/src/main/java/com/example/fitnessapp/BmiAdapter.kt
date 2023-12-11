package com.example.fitnessapp

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.databinding.ActivityStartBinding
import com.example.fitnessapp.databinding.HistoryItemBinding

class BmiAdapter(private val items: ArrayList<bmiEntry>) :
    RecyclerView.Adapter<BmiAdapter.myViewHolder>() {
    class myViewHolder(binding: HistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val main = binding.itemMain
        val serialNo = binding.serialNo
        val dateEntry = binding.date
        val bmiBox = binding.bmi
        val view = binding.view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        return myViewHolder(
            HistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        holder.bmiBox.visibility = View.VISIBLE
        holder.view.visibility = View.VISIBLE
        val date = items[position].time.toString()
        Log.d("hello", "onBindViewHolder: ${items[position].height.toString()}")
        holder.dateEntry.text = date
        holder.bmiBox.text = items[position].bmi

        holder.serialNo.text = (position + 1).toString()

        if (position % 2 == 0) {
            holder.main.setBackgroundColor(
                Color.parseColor("#DCDCDF")
            )

        } else {
            holder.main.setBackgroundColor(
                Color.parseColor("#FFFFFF")
            )
        }
    }
}