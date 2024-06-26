package com.example.fitnessapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Database
import com.example.fitnessapp.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    private lateinit var bindLogin: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: DatabaseReference
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindLogin = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bindLogin.root)
        auth = Firebase.auth
        sharedPreferences = getSharedPreferences("USERDATA", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        if (sharedPreferences.contains("email") && sharedPreferences.contains("pass")) {
            val email = sharedPreferences.getString("email", null)
            val pass = sharedPreferences.getString("pass", null)
            if (!email.isNullOrEmpty() && !pass.isNullOrEmpty()) {
                AuthenticateUser(email, pass)
            }
        }

        bindLogin.frgPassword.setOnClickListener {
            if (bindLogin.email.text.toString().isNotEmpty()) {
                Firebase.auth.sendPasswordResetEmail(bindLogin.email.text.toString())
                    .addOnSuccessListener {
                        Log.d("hello", "Email sent.")
                        val bar = Snackbar.make(
                            bindLogin.root, "Reset mail sent", Snackbar.LENGTH_SHORT
                        )
                        bar.setBackgroundTint(Color.parseColor("#0D4C92"))
                        bar.setAction("OK", View.OnClickListener {
                            bar.dismiss()
                        })
                        bar.setActionTextColor(Color.parseColor("#59C1BD"))
                        bar.show()
                    }.addOnFailureListener {
                        Log.d("hello", "Failed")
                        val bar = Snackbar.make(
                            bindLogin.root, "${it.message}", Snackbar.LENGTH_SHORT
                        )
                        bar.setBackgroundTint(Color.parseColor("#0D4C92"))
                        bar.setAction("OK", View.OnClickListener {
                            bar.dismiss()
                        })
                        bar.setActionTextColor(Color.parseColor("#59C1BD"))
                        bar.show()
                    }
            }
        }
        db = FirebaseDatabase.getInstance().getReference("Users")
        bindLogin.LoginBtn.setOnClickListener {
            val emailAddress: String = bindLogin.email.text.toString()
            val pass: String = bindLogin.password.text.toString()
            if (emailAddress.isNotBlank() && pass.isNotBlank() && emailAddress.length >= 10 &&
                pass.length >= 8 && bindLogin.checkbtn.isChecked
            ) {
                auth.signInWithEmailAndPassword(emailAddress, pass)
                    .addOnCompleteListener { it ->
                        val user = auth.currentUser
                        if (it.isSuccessful) {
                            if (user != null && user.isEmailVerified) {
                                db.child(user.uid).get().addOnSuccessListener { data ->

                                    editor.putString(
                                        "email",
                                        data.child("email").value.toString()
                                    )
                                    editor.putString(
                                        "username",
                                        data.child("userid").value.toString()
                                    )
                                    editor.putString(
                                        "pass", pass
                                    )

                                    editor.commit()

                                }
                                val intent = Intent(this, SelectionActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val bar = Snackbar.make(
                                    bindLogin.root,
                                    "Verify Email",
                                    Snackbar.LENGTH_SHORT
                                )
                                bar.setBackgroundTint(Color.parseColor("#001780"))
                                bar.setAction("Ok") {
                                    bar.dismiss()

                                }
                                bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                                bar.show()
                                sendEmailVerification()
                            }
                        } else {
                            when (it.exception) {
                                is FirebaseAuthInvalidUserException -> {
                                    val bar = Snackbar.make(
                                        bindLogin.root,
                                        "Email is not Register",
                                        Snackbar.LENGTH_SHORT
                                    )
                                    bar.setBackgroundTint(Color.parseColor("#001780"))
                                    bar.setAction("Ok") {
                                        bar.dismiss()

                                    }
                                    bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                                    bar.show()
                                }

                                else -> {
                                    val bar = Snackbar.make(
                                        bindLogin.root,
                                        "Invalid Credential",
                                        Snackbar.LENGTH_SHORT
                                    )
                                    bar.setBackgroundTint(Color.parseColor("#001780"))
                                    bar.setAction("Ok") {
                                        bar.dismiss()

                                    }
                                    bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                                    bar.show()
                                    Log.d("hello", "Auth ${it.exception}")
                                }
                            }
                        }
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
                bar.setAction("Ok") {
                    bar.dismiss()

                }
                bar.setActionTextColor(Color.parseColor("#FDFDFD"))
                bar.show()
            }

    }

    private fun AuthenticateUser(emailAddress: String, pass: String) {
        auth.signInWithEmailAndPassword(emailAddress, pass)
            .addOnCompleteListener { it ->
                val user = auth.currentUser
                if (it.isSuccessful) {
                    if (user != null && user.isEmailVerified) {
                        db.child(user.uid).get().addOnSuccessListener { data ->

                        }
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
    }
    
    
}