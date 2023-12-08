package com.example.fitnessapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.fitnessapp.databinding.ActivitySignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.make
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {
    lateinit var bindsignup: ActivitySignupBinding
    lateinit var db: DatabaseReference
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindsignup = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(bindsignup?.root)
        auth = FirebaseAuth.getInstance()
        bindsignup.SignupBtn.setOnClickListener {
            val emailAddress: String = bindsignup?.email?.text.toString()
            val pass: String = bindsignup?.password?.text.toString()
            val username: String = bindsignup?.username?.text.toString()
            if (username.isNotBlank() && emailAddress.isNotBlank() && pass.isNotBlank() && emailAddress.length >= 10 &&
                pass.length >= 8 && username.length >= 8 && bindsignup?.checkbtn?.isChecked == true
            ) {
                signUpUser(emailAddress, pass)
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun signUpUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendEmailVerification()
                } else {
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Invalid email or password format
                            Log.d("hello", "Invalid email or password format: ${exception.message}")
                            // Example: Display an error message to the user

                            var bar = make(bindsignup.root, "Invalid format", Snackbar.LENGTH_SHORT)
                            bar.setBackgroundTint(Color.parseColor("#0D4C92"))
                            bar.setAction("OK", View.OnClickListener {
                                bar.dismiss()
                            })
                            bar.setActionTextColor(Color.parseColor("#59C1BD"))
                            bar.show()
                        }

                        is FirebaseAuthUserCollisionException -> {
                            // The email address is already in use by another account
                            Log.d("hello", "Email address already in use: ${exception.message}")
                            // Example: Display an error message to the user
                            var bar =
                                make(bindsignup.root, "Email Already Exist", Snackbar.LENGTH_SHORT)
                            bar.setBackgroundTint(Color.parseColor("#0D4C92"))
                            bar.setAction("OK", View.OnClickListener {
                                bar.dismiss()
                            })
                            bar.setActionTextColor(Color.parseColor("#59C1BD"))
                            bar.show()
                        }

                        is FirebaseAuthInvalidUserException -> {
                            // User is disabled, deleted, or has an invalid credential
                            Log.d("hello", "Invalid user: ${exception.message}")
                            // Example: Display an error message to the user
                            var bar =
                                make(bindsignup.root, "Invalid credential", Snackbar.LENGTH_SHORT)
                            bar.setBackgroundTint(Color.parseColor("#0D4C92"))
                            bar.setAction("OK", View.OnClickListener {
                                bar.dismiss()
                            })
                            bar.setActionTextColor(Color.parseColor("#59C1BD"))
                            bar.show()
//                            showErrorDialog("Invalid user account.")
                        }

                        is FirebaseNetworkException -> {
                            // Handle network-related issues
                            val errorMessage = exception.message
                            // Display appropriate error message to the user
                            var bar = make(bindsignup.root, "Network issue", Snackbar.LENGTH_SHORT)
                            bar.setBackgroundTint(Color.parseColor("#0D4C92"))
                            bar.setAction("OK", View.OnClickListener {
                                bar.dismiss()
                            })
                            bar.setActionTextColor(Color.parseColor("#59C1BD"))
                            bar.show()
                        }

                        else -> {
                            // Other sign-up errors
                            Log.d("hello", "Sign-up failed: ${exception?.message}")
                            // Example: Display a generic error message to the user
                            var bar = make(bindsignup.root, "Else ‼️", Snackbar.LENGTH_SHORT)
                            bar.setBackgroundTint(Color.parseColor("#0D4C92"))
                            bar.setAction("OK", View.OnClickListener {
                                bar.dismiss()
                            })
                            bar.setActionTextColor(Color.parseColor("#59C1BD"))
                            bar.show()
//                            showErrorDialog("An error occurred during sign-up. Please try again later.")
                        }
                    }
                }
            }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser

        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    var bar = make(bindsignup.root, "Mail Sent", Snackbar.LENGTH_SHORT)
                    bar.setBackgroundTint(Color.parseColor("#0D4C92"))
                    bar.setAction("OK", View.OnClickListener {
                        bar.dismiss()

                    })
                    bar.setActionTextColor(Color.parseColor("#59C1BD"))
                    bar.show()
                    var mainname = bindsignup.username.text.toString()

                    db = FirebaseDatabase.getInstance().getReference("Users")
                    var trimuid = user.uid.substring(0, 3)
                    var trimname = mainname
                    var username = "fire${trimname}${trimuid}"
                    var entry = Users(bindsignup.email.text.toString(), mainname, username)
                    db.child(user.uid).setValue(entry).addOnSuccessListener {

                        intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("main", username)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val exception = task.exception
                    // Email verification sending failed
                }
            }?.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}