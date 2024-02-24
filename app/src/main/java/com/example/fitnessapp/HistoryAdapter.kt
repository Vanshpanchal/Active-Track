package com.example.fitnessapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.databinding.HistoryItemBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class HistoryAdapter(private val items: ArrayList<ActivtyEntry>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    lateinit var mylistener: onitemclick

    interface onitemclick {
        fun itemClickListener(position: Int)
    }

    fun onItem(listener: onitemclick) {
        mylistener = listener
    }

    class ViewHolder(binding: HistoryItemBinding, listener: onitemclick) :
        RecyclerView.ViewHolder(binding.root) {

        val main = binding.itemMain
        val serialNo = binding.serialNo
        val dateEntry = binding.date
        val one = binding.bmi
        val two = binding.view
        val act_logo = binding.actLogo

        init {
            itemView.setOnClickListener {
                listener.itemClickListener(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), mylistener
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val date: Int = items[position]
        val date = items[position].Date
        val obj = date?.toDate()
        val timeZone = ZoneId.of("UTC")
        val localDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(obj?.time!!), timeZone)
        val year = localDateTime.year.toString()
        val month = localDateTime.month.getDisplayName(
            TextStyle.SHORT,
            Locale.getDefault()
        ).toString()
        val day = localDateTime.dayOfMonth.toString()
        val exercise_date = "$day $month $year"
        holder.dateEntry.text = exercise_date
        holder.one.visibility = View.GONE
        holder.two.visibility = View.GONE

        val repDuration = items[position].ExerciseDuration?.toInt()
        if (repDuration != null) {
            if(repDuration>=30){
                holder.act_logo.setImageResource(R.drawable.fit_icon)
            }else{
                holder.act_logo.setImageResource(R.drawable.meditation)
            }
        }
        holder.serialNo.text = (position + 1).toString()

        if (position % 2 == 0) {
            holder.main.setBackgroundResource(R.drawable.item_bg)


        } else {
            holder.main.setBackgroundResource(R.drawable.item_bg2)
        }

    }
}