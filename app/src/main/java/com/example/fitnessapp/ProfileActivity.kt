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
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.fitnessapp.databinding.ActivityProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var DataList: ArrayList<Menu>
    private lateinit var bindProfile: ActivityProfileBinding
    private lateinit var storageReference: StorageReference
    private lateinit var db: DatabaseReference
    var BmiValue = ""
    var HeightValue = ""
    var WeightValue = ""
    private lateinit var fs: FirebaseFirestore
    private lateinit var emailAddress: String
    private lateinit var profileDataList: ArrayList<profileEntry>
    private lateinit var username: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sharedPreferencesHealth: SharedPreferences
    private lateinit var editorHealth: SharedPreferences.Editor
    private var galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val loading = custom_prog(this)
                loading.start()
                storageReference =
                    FirebaseStorage.getInstance().getReference("Users/" + auth.currentUser?.uid)
                storageReference.putFile(result.data?.data!!).addOnSuccessListener {
                    Log.d("hello", "onCreate: Uploaded")
                    galleryprofile()
                    loading.dismiss()
                }.addOnFailureListener {
                    Log.d("hello", "onCreate: Failed")

                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindProfile.root)
        profileDataList = arrayListOf()
        profileDataList.sortByDescending {
            it.timestamp
        }
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
        fs = FirebaseFirestore.getInstance()
        val i = 1
        fs.collection("BMI").document(user?.uid!!).collection("MyBMI").orderBy("timestamp").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    for (data in it) {
                        val obj = data.toObject(profileEntry::class.java)
                        profileDataList.add(obj)
                        profileDataList.sortByDescending {
                            it.timestamp
                        }
                    }
                    BmiValue = profileDataList[0].bmi.toString()
                    HeightValue = profileDataList[0].height.toString()
                    WeightValue = profileDataList[0].weight.toString()
                    Log.d("hello", "onCreate: $WeightValue")
                    sharedPreferencesHealth =
                        applicationContext.getSharedPreferences("HEALTH-DATA", Context.MODE_PRIVATE)
                    editorHealth = sharedPreferences.edit()
                    bindProfile.bmiValue.text =
                        sharedPreferencesHealth.getString("Bmi", BmiValue).toString()
                    bindProfile.heightValue.text =
                        sharedPreferencesHealth.getString("height", HeightValue).toString()
                    bindProfile.weightValue.text =
                        sharedPreferencesHealth.getString("weight", WeightValue).toString()
                    Log.d("hello", "onCreate: g$WeightValue")
                    editorHealth.commit()
                }

            }

//        sharedPreferencesHealth =
//            applicationContext.getSharedPreferences("HEALTH-DATA", Context.MODE_PRIVATE)
//        editorHealth = sharedPreferences.edit()
//        bindProfile.bmiValue.text = sharedPreferencesHealth.getString("Bmi", BmiValue).toString()
//        bindProfile.heightValue.text =
//            sharedPreferencesHealth.getString("height", HeightValue).toString()
//        bindProfile.weightValue.text =
//            sharedPreferencesHealth.getString("weight", WeightValue).toString()
//        Log.d("hello", "onCreate: g$WeightValue")
//        editorHealth.commit()

//        LoadData(user!!)

        profileImage()


        val MenuName: Array<String> =
            arrayOf("HISTORY", "EDIT PROFILE PIC", "RESET PASSWORD", "SUPPORT", "LOGOUT")
        val IconID: Array<Int> = arrayOf(
            R.drawable.baseline_history_24,
            R.drawable.baseline_account_circle_24,
            R.drawable.baseline_lock_reset_24,
            R.drawable.baseline_support_agent_24,
            R.drawable.baseline_logout_24
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
                    i -> {
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
                                    val bar = Snackbar.make(
                                        bindProfile.root, "Reset mail sent", Snackbar.LENGTH_SHORT
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
                                        bindProfile.root, "${it.message}", Snackbar.LENGTH_SHORT
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


    private fun profileImage() {
        bindProfile.profileShimmer.startShimmerAnimation()
        val ShimmerHandler = Handler(Looper.getMainLooper())
        ShimmerHandler.postDelayed({
            bindProfile.profile.visibility = View.VISIBLE
            bindProfile.profileShimmer.stopShimmerAnimation()
            bindProfile.profileShimmer.visibility = View.GONE

        }, 800)
        val user = auth.currentUser
        storageReference = FirebaseStorage.getInstance().reference.child("Users/${user?.uid}")
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(bindProfile.profilePic)
        }.addOnFailureListener { exception -> }
    }

    fun profile() {
        bindProfile.profileShimmer.startShimmerAnimation()

        val ShimmerHandler = Handler(Looper.getMainLooper())
        ShimmerHandler.postDelayed({
            bindProfile.profile.visibility = View.VISIBLE
            bindProfile.profileShimmer.stopShimmerAnimation()
            bindProfile.profileShimmer.visibility = View.GONE
        }, 800)

        val user = auth.currentUser
        storageReference = FirebaseStorage.getInstance().reference.child("Users/${user?.uid}")

        storageReference.downloadUrl.addOnSuccessListener { uri ->
            // Start shimmer again if needed
            bindProfile.profileShimmer.startShimmerAnimation()

            // Load image using Glide
            Glide.with(this).load(uri).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Stop shimmer and handle failure if needed
                    bindProfile.profileShimmer.stopShimmerAnimation()
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
                    bindProfile.profileShimmer.stopShimmerAnimation()
                    bindProfile.profileShimmer.visibility = View.GONE
                    return false
                }
            }).into(bindProfile.profilePic)
        }.addOnFailureListener { exception ->
            // Handle failure if needed
            bindProfile.profileShimmer.stopShimmerAnimation()
        }
    }

    fun galleryprofile() {
        bindProfile.profileShimmer.startShimmerAnimation()

        val user = auth.currentUser
        storageReference = FirebaseStorage.getInstance().reference.child("Users/${user?.uid}")

        storageReference.downloadUrl.addOnSuccessListener { uri ->
            // Start shimmer again if needed
            bindProfile.profileShimmer.startShimmerAnimation()

            // Load image using Glide
            Glide.with(this).load(uri).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Stop shimmer and handle failure if needed
                    bindProfile.profileShimmer.stopShimmerAnimation()
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
                    bindProfile.profileShimmer.stopShimmerAnimation()
                    bindProfile.profileShimmer.visibility = View.GONE
                    return false
                }
            }).into(bindProfile.profilePic)
        }.addOnFailureListener { exception ->
            // Handle failure if needed
            bindProfile.profileShimmer.stopShimmerAnimation()
        }
    }

    private fun checkpermissionRead() = ActivityCompat.checkSelfPermission(
        this, Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkpermissionReadImages() = ActivityCompat.checkSelfPermission(
        this, Manifest.permission.READ_MEDIA_IMAGES
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
////                profileImage()
//                profile()
            }

            if (permissiontoRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissiontoRequest.toTypedArray(), 0)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("hello", "onRequestPermissionsResult: Done")

                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        profile()
    }


}