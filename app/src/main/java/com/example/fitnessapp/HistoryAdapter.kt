package com.example.fitnessapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.databinding.ActivityHistoryBinding
import com.example.fitnessapp.databinding.HistoryItemBinding

class HistoryAdapter(private val items: ArrayList<String>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(binding: HistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {

        val main = binding.itemMain
        val serialNo = binding.serialNo
        val dateEntry = binding.date
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date: String = items.get(position)
        holder.dateEntry.text = date
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