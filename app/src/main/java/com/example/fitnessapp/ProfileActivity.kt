package com.example.fitnessapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.fitnessapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private var galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                storageReference =
                    FirebaseStorage.getInstance().getReference("Users/" + auth.currentUser?.uid)
                storageReference.putFile(result.data?.data!!).addOnSuccessListener {
                    Log.d("hello", "onCreate: Uploaded")
                    profileImage()
                }.addOnFailureListener {
                    Log.d("hello", "onCreate: Failed")

                }
                profileImage()
            }
        }
    private lateinit var bindProfile: ActivityProfileBinding
    private lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindProfile.root)
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        profileImage()
        bindProfile.btn.setOnClickListener {
            if (user != null) {
                Log.d("hello", "onCreate: ${user}")
                requestpermission()
                profileImage()
            }
        }
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

    private fun profileImage() {
        val user = auth.currentUser
        storageReference =
            FirebaseStorage.getInstance().reference.child("Users/${user?.uid}")
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(bindProfile.profilePic)
        }.addOnFailureListener { exception -> }
    }

    private fun checkpermissionRead() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkpermissionReadImages() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestpermission() {
        val permissiontoRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 33) {
            if (!checkpermissionReadImages()) {
                permissiontoRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
            }

            if (permissiontoRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissiontoRequest.toTypedArray(), 0)
            }
        } else {
            if (!checkpermissionRead()) {
                permissiontoRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
                profileImage()
            }

            if (permissiontoRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissiontoRequest.toTypedArray(), 0)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("hello", "onRequestPermissionsResult: Done")
                    profileImage()
                }
            }
        }
    }


}