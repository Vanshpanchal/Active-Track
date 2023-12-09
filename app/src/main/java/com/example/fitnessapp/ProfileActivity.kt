package com.example.fitnessapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.request.target.Target
import com.example.fitnessapp.databinding.ActivityProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var DataList: ArrayList<Menu>
    private lateinit var bindProfile: ActivityProfileBinding
    private lateinit var storageReference: StorageReference
    private lateinit var db: DatabaseReference
    private lateinit var emailAddress: String
    private lateinit var username: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindProfile.root)
        auth = FirebaseAuth.getInstance()
        sharedPreferences =
            applicationContext.getSharedPreferences("USERDATA", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        Log.d("hello", "onCreate: ${sharedPreferences.all}")
        emailAddress = sharedPreferences.getString("email", "").toString()
        username = sharedPreferences.getString("username", "").toString()
        bindProfile.email.text = emailAddress
        bindProfile.username.text = username
        editor.commit()
        val user = auth.currentUser
//        LoadData(user!!)
        profileImage()
        bindProfile.btn.setOnClickListener {
            if (user != null) {
                Log.d("hello", "onCreate: ${user}")
                requestpermission()
                profileImage()
            }
        }

        val MenuName: Array<String> =
            arrayOf("HISTORY", "EDIT PROFILE PIC", "RESET PASSWORD", "SUPPORT", "LOGOUT")
        val IconID: Array<Int> = arrayOf(
            R.drawable.baseline_history_24,
            R.drawable.baseline_account_circle_24,
            R.drawable.baseline_lock_reset_24,
            R.drawable.baseline_support_agent_24, R.drawable.baseline_logout_24
        )
        DataList = arrayListOf()
        for (index in MenuName.indices) {
            val entry = Menu(MenuName[index], IconID[index])
            DataList.add(entry)
        }

        bindProfile.Menus.layoutManager = LinearLayoutManager(this)
        val adapter = MenuAdapter(DataList, this)
        bindProfile.Menus.adapter = adapter
        adapter.onitem(object : MenuAdapter.onitemclick {
            override fun itemclicklistener(position: Int) {
                when (position) {
                    1 -> {
                        if (user != null) {
                            Log.d("hello", "onCreate: ${user}")
                            requestpermission()
                            profileImage()
                        }
                    }

                    0 -> {
                        intent = Intent(this@ProfileActivity, HistoryActivity::class.java)
                        startActivity(intent)
                    }

                    2 -> {
                        if (bindProfile.email.text.toString().isNotEmpty()) {
                            Firebase.auth.sendPasswordResetEmail(bindProfile.email.text.toString())
                                .addOnSuccessListener {
                                    Log.d("hello", "Email sent.")
                                    val bar =
                                        Snackbar.make(
                                            bindProfile.root,
                                            "Reset mail sent",
                                            Snackbar.LENGTH_SHORT
                                        )
                                    bar.setBackgroundTint(Color.parseColor("#0D4C92"))
                                    bar.setAction("OK", View.OnClickListener {
                                        bar.dismiss()
                                    })
                                    bar.setActionTextColor(Color.parseColor("#59C1BD"))
                                    bar.show()
                                }.addOnFailureListener {
                                    Log.d("hello", "Failed")
                                    val bar =
                                        Snackbar.make(
                                            bindProfile.root,
                                            "${it.message}",
                                            Snackbar.LENGTH_SHORT
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

                    4 -> {
                        editor.clear()
                        editor.commit()
                        auth.signOut()
                        intent = Intent(this@ProfileActivity, WelcomeActivity::class.java)
                        startActivity(intent)

                    }

                    else -> {
                        Log.d("hello", "itemclicklistener: Clicked")
                        db = FirebaseDatabase.getInstance().getReference("Users")
                        db.child(user?.uid!!).get().addOnSuccessListener {
                            Log.d("hello", "itemclicklistener: ${it.child("userid").value}")

                        }
                    }
                }
            }

        })


    }

    private fun LoadData(user: FirebaseUser) {
        db = FirebaseDatabase.getInstance().getReference("Users")
        db.child(user?.uid!!).get().addOnSuccessListener {

            bindProfile.email.text = emailAddress
            bindProfile.username.text = it.child("userid").value.toString()
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