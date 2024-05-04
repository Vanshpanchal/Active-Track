package com.example.fitnessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.fitnessapp.databinding.ActivityAboutBinding

class about_act : AppCompatActivity() {

    lateinit var   bindabout : ActivityAboutBinding;
    override fun onCreate(savedInstanceState: Bundle?) {
        bindabout = ActivityAboutBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(bindabout.root)

        setSupportActionBar(bindabout.actionbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = ("ABOUT US")

        }
        bindabout.actionbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

//        bindabout?.scView?.setOnScrollChangeListener(object : View.OnScrollChangeListener {
//            override fun onScrollChange(
//                v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int
//            ) {
//                bindabout?.linearlayout2?.setPadding(0, 0, 0, 20)
//            }
//
//        })
    }
}