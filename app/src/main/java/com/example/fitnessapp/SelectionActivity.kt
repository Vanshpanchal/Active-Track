package com.example.fitnessapp

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.fitnessapp.databinding.ActivitySelectionBinding
import com.example.fitnessapp.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class SelectionActivity : AppCompatActivity() {
    lateinit var bindSelect: ActivitySelectionBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var StorageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        bindSelect = ActivitySelectionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(bindSelect.root)

        auth = FirebaseAuth.getInstance()
        profileImage()

        bindSelect.workout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        bindSelect.profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


    }

    private fun profileImage() {
        val user = auth.currentUser
        StorageReference =
            FirebaseStorage.getInstance().reference.child("Users/${user?.uid}")
        StorageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(bindSelect.profileImg)
        }.addOnFailureListener { exception -> }
    }


    override fun onResume() {
        super.onResume()
        profileImage()
    }

    override fun onRestart() {
        super.onRestart()
        profileImage()
    }

    override fun onPause() {
        super.onPause()
        profileImage()
    }
}