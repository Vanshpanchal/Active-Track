package com.example.fitnessapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessapp.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var bindLogin: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindLogin = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bindLogin.root)

        auth = Firebase.auth
        bindLogin.LoginBtn.setOnClickListener {
            val emailAddress: String = bindLogin.email.text.toString()
            val pass: String = bindLogin.password.text.toString()
            if (emailAddress.isNotBlank() && pass.isNotBlank() && emailAddress.length >= 10 &&
                pass.length >= 8 && bindLogin.checkbtn.isChecked
            ) {
                auth.signInWithEmailAndPassword(emailAddress, pass)
                    .addOnCompleteListener {
                        val user = auth.currentUser
                        if (it.isSuccessful) {
                            if (user != null && user.isEmailVerified) {
                                val intent = Intent(this, SelectionActivity::class.java)
                                startActivity(intent)
                            } else {
                                val bar = Snackbar.make(
                                    bindLogin.root,
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
                            bindLogin.root,
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

            } else {
                if (!bindLogin.checkbtn.isChecked) {
                    val bar = Snackbar.make(
                        bindLogin.root,
                        "Please Check The Terms & Conditions",
                        Snackbar.LENGTH_SHORT
                    )
                    bar.setBackgroundTint(Color.parseColor("#001780"))
                    bar.setAction("OK") {
                        bar.dismiss()
                    }
                    bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                    bar.show()
                } else {
                    val bar = Snackbar.make(
                        bindLogin.root,
                        "Please Check Your Entered Credentials",
                        Snackbar.LENGTH_SHORT
                    )
                    bar.setBackgroundTint(Color.parseColor("#001780"))
                    bar.setAction("OK") {
                        bar.dismiss()
                    }
                    bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                    bar.show()
                    Log.d("hello", "Failed")
                }

            }
        }

    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener {
                val bar = Snackbar.make(bindLogin.root, "Mail Sent", Snackbar.LENGTH_SHORT)
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
                    Snackbar.make(bindLogin.root, it.message.toString(), Snackbar.LENGTH_SHORT)
                bar.setBackgroundTint(Color.parseColor("#001780"))
                bar.setAction("OK") {
                    bar.dismiss()
                }
                bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                bar.show()
            }
    }
}