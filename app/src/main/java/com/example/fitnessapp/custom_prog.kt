package com.example.fitnessapp

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import com.example.fitnessapp.databinding.ConfirmationDialogBinding

class custom_prog(val act : Activity) {
    private lateinit var isDialog : AlertDialog
    fun start()
    {
        val infalter = act.layoutInflater
        val dialogView = infalter.inflate(R.layout.custom_progress,null)
        /**set Dialog*/
        val bulider = AlertDialog.Builder(act)

        bulider.setView(dialogView)
        bulider.setCancelable(false)
        isDialog = bulider.create()
        isDialog.show()
    }

    fun dismiss()
    {
        isDialog.dismiss()
    }
}