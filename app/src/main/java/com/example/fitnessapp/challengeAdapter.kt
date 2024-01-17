package com.example.fitnessapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.databinding.ActivityBmiBinding
import com.example.fitnessapp.databinding.ChallengeDailogBinding
import com.example.fitnessapp.databinding.HistoryItemBinding

class challengeAdapter(private val items: ArrayList<challengeEntry>) :
    RecyclerView.Adapter<challengeAdapter.ViewHolder>() {
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
        val entry = binding.date
        val one = binding.bmi
        val two = binding.view

        init {
            itemView.setOnClickListener {
                listener.itemClickListener(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): challengeAdapter.ViewHolder {
        return challengeAdapter.ViewHolder(
            HistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), mylistener
        )
    }

    override fun onBindViewHolder(holder: challengeAdapter.ViewHolder, position: Int) {
//        val obj = date?.toDate().toString()
//        val str = obj.substringBefore("G")
//        holder.dateEntry.text = str
        holder.one.visibility = View.GONE
        holder.two.visibility = View.GONE
        holder.serialNo.text = (position + 1).toString()
        holder.entry.text = "Day ${(position + 1)}"
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

    override fun getItemCount(): Int {
        return items.size

    }

}