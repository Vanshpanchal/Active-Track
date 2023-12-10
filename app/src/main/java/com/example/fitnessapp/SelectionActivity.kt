package com.example.fitnessapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
//        profileImage()

        profile()
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

    fun profile() {
        bindSelect.profileShimmer.startShimmerAnimation()

        val ShimmerHandler = Handler(Looper.getMainLooper())
        ShimmerHandler.postDelayed({
            bindSelect.profile.visibility = View.VISIBLE
            bindSelect.profileShimmer.stopShimmerAnimation()
            bindSelect.profileShimmer.visibility = View.GONE
        }, 800)

        val user = auth.currentUser
        StorageReference = FirebaseStorage.getInstance().reference.child("Users/${user?.uid}")

        StorageReference.downloadUrl.addOnSuccessListener { uri ->
            // Start shimmer again if needed
            bindSelect.profileShimmer.startShimmerAnimation()

            // Load image using Glide
            Glide.with(this)
                .load(uri)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Stop shimmer and handle failure if needed
                        bindSelect.profileShimmer.stopShimmerAnimation()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Image loaded, stop shimmer
                        bindSelect.profileShimmer.stopShimmerAnimation()
                        bindSelect.profileShimmer.visibility = View.GONE
                        return false
                    }
                })
                .into(bindSelect.profileImg)
        }.addOnFailureListener { exception ->
            // Handle failure if needed
            bindSelect.profileShimmer.stopShimmerAnimation()
        }
    }

}