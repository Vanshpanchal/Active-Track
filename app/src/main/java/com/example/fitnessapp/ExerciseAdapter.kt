package com.example.fitnessapp

import android.graphics.ColorSpace.Model
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.databinding.ExerciseStatusBinding

class ExerciseAdapter(val items: ArrayList<ExerciseModel>) :
    RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

    class ViewHolder(binding: ExerciseStatusBinding) : RecyclerView.ViewHolder(binding.root) {
        val exerciseNumber = binding.exerciseNumber
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ExerciseStatusBinding.inflate(
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
        val model: ExerciseModel = items[position]
//        holder.exerciseNumber.text = model.getId().toString()

        when {
            model.getIsSelected() -> {
                holder.exerciseNumber.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.status_selected_bg
                )

            }

            model.getIsCompleted() -> {
                holder.exerciseNumber.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.status_completed_bg                )
            }

            else -> {
                holder.exerciseNumber.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.status_bg
                )
            }
        }
    }
}