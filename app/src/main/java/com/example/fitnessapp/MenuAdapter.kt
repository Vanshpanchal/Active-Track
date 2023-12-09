package com.example.fitnessapp

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView

class MenuAdapter(var menuList: ArrayList<Menu>, var context: Activity) :
    RecyclerView.Adapter<MenuAdapter.MyViewHolder>() {
    lateinit var mylistener: onitemclick

    interface onitemclick {
        fun itemclicklistener(position: Int)
    }

    fun onitem(listener: onitemclick) {
        mylistener = listener
    }
    class MyViewHolder(itemView: View, listener: onitemclick) : RecyclerView.ViewHolder(itemView) {
        val icon = itemView.findViewById<ImageView>(R.id.icon)
        val menuName = itemView.findViewById<TextView>(R.id.name)

        init {
            itemView.setOnClickListener {

                listener.itemclicklistener(adapterPosition)
            }
        }

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.menustyle, parent, false)
        return MyViewHolder(itemView, mylistener)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentInstance = menuList[position]
        holder.icon.setImageResource(currentInstance.icon)
        holder.menuName.text = currentInstance.name

    }
}