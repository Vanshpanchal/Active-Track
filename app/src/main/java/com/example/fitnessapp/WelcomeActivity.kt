package com.example.fitnessapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.example.fitnessapp.databinding.ActivityFinishBinding
import com.example.fitnessapp.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private var bindWelcome: ActivityWelcomeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindWelcome = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(bindWelcome?.root)

        bindWelcome?.loginBtn?.setOnClickListener {
            Log.d("hello", "LOGIN")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        bindWelcome?.signupBtn?.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            Log.d("hello", "SIGNUP")
        }
    }
}