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

    lateinit var mylistener: onitemclickB

    interface onitemclickB {
        fun itemClickListener(position: Int)
    }

    fun onItem(listener: onitemclickB) {
        mylistener = listener
    }

    class myViewHolder(binding: HistoryItemBinding, listener: onitemclickB) :
        RecyclerView.ViewHolder(binding.root) {
        val main = binding.itemMain
        val serialNo = binding.serialNo
        val dateEntry = binding.date
        val bmiBox = binding.bmi
        val view = binding.view
        val act_logo = binding.actLogo

        init {
            itemView.setOnClickListener {
                listener.itemClickListener(adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        return myViewHolder(
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

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        holder.bmiBox.visibility = View.VISIBLE
        holder.view.visibility = View.VISIBLE

        val date = items[position].time.toString()
        Log.d("hello", "onBindViewHolder: ${items[position].height.toString()}")
        holder.dateEntry.text = date
        holder.bmiBox.text = items[position].bmi

        holder.serialNo.text = (position + 1).toString()

        val bmi_value = items[position].bmi?.toFloat()
        if (bmi_value != null) {
            if (bmi_value < 18.5) {

                holder.act_logo.setImageResource(R.drawable.bluecircle)

            } else if (bmi_value > 30.0) {
//                holder.bmiBox.setBackgroundColor(
//                    Color.parseColor("#b41818")
//                )
                holder.act_logo.setImageResource(R.drawable.redcircle)
            } else if (bmi_value in 25.0..30.0) {

                holder.act_logo.setImageResource(R.drawable.yellowcircle)
            } else {

                holder.act_logo.setImageResource(R.drawable.greencircle)
            }
        }
        if (position % 2 == 0) {
            holder.main.setBackgroundResource(R.drawable.item_bg)


        } else {
            holder.main.setBackgroundResource(R.drawable.item_bg2)
        }
    }
}