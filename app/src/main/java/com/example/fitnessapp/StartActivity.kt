package com.example.fitnessapp

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.fitnessapp.databinding.ActivityStartBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase

class StartActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var bindstart: ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        bindstart = ActivityStartBinding.inflate(layoutInflater)
        setContentView(bindstart.root)

        auth = Firebase.auth
        sharedPreferences = getSharedPreferences("USERDATA", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        if (sharedPreferences.contains("email") && sharedPreferences.contains("pass")) {
            val email = sharedPreferences.getString("email", null)
            val pass = sharedPreferences.getString("pass", null)
            if (!email.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                AuthenticateUser(email, pass)
            }
        } else {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun AuthenticateUser(emailAddress: String, pass: String) {
        auth.signInWithEmailAndPassword(emailAddress, pass)
            .addOnCompleteListener { it ->
                val user = auth.currentUser
                if (it.isSuccessful) {
                    if (user != null && user.isEmailVerified) {

                        val intent = Intent(this, SelectionActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        val bar = Snackbar.make(
                            bindstart.root,
                            "Verify Email",
                            Snackbar.LENGTH_SHORT
                        )
                        bar.setBackgroundTint(Color.parseColor("#001780"))
                        bar.setAction("OK") {
                            bar.dismiss()
                        }
                        bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                        bar.show()
                        sendEmailVerification()
                    }
                } else {
                    Log.d("hello", "onCreate: ${it.exception?.message} ")
                }
            }.addOnFailureListener {
                val bar = Snackbar.make(
                    bindstart.root,
                    "Please Check Your Entered Credentials",
                    Snackbar.LENGTH_SHORT
                )
                bar.setBackgroundTint(Color.parseColor("#001780"))
                bar.setAction("OK") {
                    bar.dismiss()
                }
                bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                bar.show()
                Log.d("hello", "onCreate: ${it.message} ")
            }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener {
                val bar = Snackbar.make(bindstart.root, "Mail Sent", Snackbar.LENGTH_SHORT)
                bar.setBackgroundTint(Color.parseColor("#001780"))
                bar.setAction("Show") {
                    bar.dismiss()
                    val intent = packageManager.getLaunchIntentForPackage("com.google.android.gm")
                    if (intent != null) {
                        // Start the Gmail app
                        startActivity(intent)
                    } else {
                        // Display a message if Gmail is not installed
                        Toast.makeText(this, "Gmail app not installed", Toast.LENGTH_SHORT).show()
                    }

                }
                bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                bar.show()
            }
            ?.addOnFailureListener {
                val bar =
                    Snackbar.make(bindstart.root, it.message.toString(), Snackbar.LENGTH_SHORT)
                bar.setBackgroundTint(Color.parseColor("#001780"))
                bar.setAction("OK") {
                    bar.dismiss()
                }
                bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                bar.show()
            }
    }
}