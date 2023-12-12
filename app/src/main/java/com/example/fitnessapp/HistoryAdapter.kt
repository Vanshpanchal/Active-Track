package com.example.fitnessapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.databinding.HistoryItemBinding

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
        holder.dateEntry.text = items[position].StartDuration
        holder.one.visibility = View.GONE
        holder.two.visibility = View.GONE
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