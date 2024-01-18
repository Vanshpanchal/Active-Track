package com.example.fitnessapp

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.fitnessapp.databinding.ActivityChallengeBinding
import com.example.fitnessapp.databinding.CustomProgressBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class challengeAct : AppCompatActivity() {
    //challenge
    private lateinit var auth: FirebaseAuth
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var view: View
    lateinit var binding: ActivityChallengeBinding
    lateinit var bottomDialog: BottomSheetDialog
    private lateinit var storageReference: StorageReference
    private lateinit var fsStore: FirebaseFirestore
    private val PREFS_NAME = "StreakPrefs"
    private val STREAK_KEY = "streak"
    private lateinit var list: ArrayList<challengeEntry>


    private val sharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Retrieve the current streak from SharedPreferences
    private fun getCurrentStreak(): Int {
        return sharedPreferences.getInt(STREAK_KEY, 0)
    }

    // Increment the streak and save the updated value to SharedPreferences
    private fun incrementStreak() {
        val currentStreak = getCurrentStreak()
        sharedPreferences.edit().putInt(STREAK_KEY, currentStreak + 1).apply()
    }

    // Reset the streak to 0 in SharedPreferences
    private fun resetStreak() {
        sharedPreferences.edit().putInt(STREAK_KEY, 0).apply()
    }

    private var galleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
//                val loading = custom_prog(this)
//                loading.start()
                val streak = getCurrentStreak()
                val dialog = Dialog(this)
                val layout = CustomProgressBinding.inflate(layoutInflater)
                dialog.setContentView(layout.root)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                fsStore.collection("Challenge").document(auth.currentUser?.uid!!)
                    .collection("mychallenge")
                    .whereEqualTo("Date", date).get().addOnSuccessListener { outcome ->
                        if (outcome.isEmpty) {
                            fsStore.collection("Streak").document(auth.currentUser?.uid!!)
                                .collection("mystreak").document(auth.currentUser?.uid!! + "Streak")
                                .get().addOnSuccessListener { it ->
                                    if (it.exists()) {
                                        val streakInt = it.getField<Int>("Streak")
                                        Log.d("75", "Loadimg: $streakInt")

                                        storageReference =
                                            FirebaseStorage.getInstance()
                                                .getReference("Challenge/" + auth.currentUser?.uid)
                                                .child(
                                                    "Day$streakInt"
                                                )
                                        storageReference.putFile(result.data?.data!!)
                                            .addOnSuccessListener {

                                                val s = getCurrentStreak()
                                                Loadimg(view, s)
                                                Log.d("hello", "onCreate: Uploaded")
                                                view.findViewById<ImageView>(R.id.prog_pic).tag =
                                                    "Done"
                                                dialog.dismiss()
                                            }
                                    }
                                }.addOnFailureListener {
                                    Log.d("hello", "onCreate: Failed")

                                }
                        } else {
                            Log.d("75", "Fitness activity for today already logged")
                            dialog.dismiss()
                        }

                    }.addOnFailureListener {
                        Log.w("75", "Error checking for existing fitness record")
                    }


            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityChallengeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding?.actionbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = (getString(R.string._75_hard_challenge))
        }
        binding.actionbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        bottomDialog = BottomSheetDialog(this)
        auth = FirebaseAuth.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        fsStore = FirebaseFirestore.getInstance()
        list = arrayListOf()
        getData()
        binding.rvChallenge.layoutManager = LinearLayoutManager(this@challengeAct)
        binding.upload.setOnClickListener {
            fsStore.collection("Challenge").document(auth.currentUser?.uid!!)
                .collection("mychallenge")
                .whereEqualTo("Date", date).get().addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        view =
                            View.inflate(this@challengeAct, R.layout.challenge_dailog, null)
                        bottomDialog.setContentView(view)
                        val img = view.findViewById<ImageView>(R.id.prog_pic)

                        img.setOnClickListener {
                            requestpermission(view)
                        }
                        val uploadbtn = view.findViewById<Button>(R.id.btn_upload)
                        uploadbtn.setOnClickListener {
                            if (img.tag != "Null") {
                                val s = getCurrentStreak()
                                uploadData(view, s)
                                incrementStreak()
                                bottomDialog.dismiss()
                            } else {
                                Log.d("75", "onCreate: Please Add Prog Image")
                            }
                        }
                        bottomDialog.show()
                    } else {
                        Log.d("75", "Fitness activity for today already logged")
                    }
                }.addOnFailureListener {
                    Log.w("75", "Error checking for existing fitness record")
                }


        }
    }

    private fun checkpermissionRead() = ActivityCompat.checkSelfPermission(
        this, Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkpermissionReadImages() = ActivityCompat.checkSelfPermission(
        this, Manifest.permission.READ_MEDIA_IMAGES
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestpermission(view: View) {
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

    fun Loadimg(view: View, Streak: Int) {
//        .profileShimmer.startShimmerAnimation()
        fsStore.collection("Streak").document(auth.currentUser?.uid!!)
            .collection("mystreak").document(auth.currentUser?.uid!! + "Streak")
            .get().addOnSuccessListener { it ->
                if (it.exists()) {
                    val streakInt = it.getField<Int>("Streak")
                    Log.d("75", "Loadimg: $streakInt")

                    val user = auth.currentUser
                    storageReference =
                        FirebaseStorage.getInstance().reference.child("Challenge/${auth.currentUser?.uid}/Day$streakInt")


                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        // Start shimmer again if needed
//            bindProfile.profileShimmer.startShimmerAnimation()

                        // Load image using Glide
                        Glide.with(this).load(uri).listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                // Stop shimmer and handle failure if needed
//                    bindProfile.profileShimmer.stopShimmerAnimation()
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
                                return false
                            }
                        }).into(view.findViewById(R.id.prog_pic))
                    }.addOnFailureListener { exception ->
                        // Handle failure if needed
//            bindProfile.profileShimmer.stopShimmerAnimation()
                    }
                }
            }
    }

    fun uploadData(view: View, streak: Int) {

        val diet = view.findViewById<CheckBox>(R.id.task_1).isChecked
        val workout = view.findViewById<CheckBox>(R.id.task_2).isChecked
        val water = view.findViewById<CheckBox>(R.id.task_3).isChecked
        val reading = view.findViewById<CheckBox>(R.id.task_4).isChecked
        val progress_pic = view.findViewById<CheckBox>(R.id.task_5).isChecked
        val learning = view.findViewById<EditText>(R.id.learning).text
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        Log.d("75", "uploadData: $diet & $workout $streak $learning")

        val data = hashMapOf(
            "Diet" to diet.toString(),
            "Workout" to workout.toString(),
            "Water" to water.toString(),
            "Reading" to reading.toString(),
            "ProgressPic" to progress_pic.toString(),
            "Learning" to learning.toString(),
            "Streak" to streak.toString(),
            "Date" to date,
            "Time" to Timestamp.now()
        )

        fsStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        fsStore.collection("Challenge").document(auth.currentUser?.uid!!).collection("mychallenge")
            .whereEqualTo("Date", date).get().addOnSuccessListener { result ->
                if (result.isEmpty) {
//                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                    val currentDate = Date()
//                    val calendar = Calendar.getInstance()
//                    calendar.time = currentDate
//                    calendar.add(Calendar.DAY_OF_YEAR, -1)
//                    val yesterdayDate = calendar.time
//                    val yesterdayFormattedDate = dateFormat.format(yesterdayDate)
//                    fsStore.collection("Challenge").document(auth.currentUser?.uid!!)
//                        .collection("mychallenge")
//                        .whereEqualTo("Date", yesterdayFormattedDate).get()
//                        .addOnSuccessListener { yesterday ->
//                            if (!yesterday.isEmpty) {
                    fsStore.collection("Challenge").document(auth.currentUser?.uid!!)
                        .collection("mychallenge")
                        .document().set(data).addOnSuccessListener {

                            fsStore.collection("Streak")
                                .document(auth.currentUser?.uid!!)
                                .collection("mystreak")
                                .document(auth.currentUser?.uid!! + "Streak")
                                .get().addOnSuccessListener { it ->
                                    if (it.exists()) {
                                        val getstreak = it.getField<Int>("Streak")
                                        if (getstreak != null) {
                                            if (getstreak == 74) {
                                                streakdata(getstreak + 1, true)
                                            } else {
                                                streakdata(getstreak + 1, false)
                                            }
                                        }
                                        Log.d("75", "Loadimg: $getstreak")
                                    }
//                                            }
                                }
//                            }else{ // hello world
//                                Log.d("75", "Streak Broken")
//
//                            }
                            Log.d("75", "uploadToStore: Done")
                            getData()
                        }
                } else {
                    Log.d("75", "Fitness activity for today already logged")
                }

            }.addOnFailureListener {
                Log.w("75", "Error checking for existing fitness record")
            }

    }

    fun getData() {
        fsStore = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()

        fsStore.collection("Challenge").document(auth.currentUser?.uid!!)
            .collection("mychallenge").get().addOnSuccessListener { it ->
                if (!it.isEmpty) {
                    list.clear()
                    for (data in it) {
                        val obj = data.toObject(challengeEntry::class.java)
                        if (obj != null) {
                            list.add(obj)
                        }
                    }
                    val rec = binding.rvChallenge
                    val adapter = challengeAdapter(list)
                    rec.adapter = adapter

                    adapter.onItem(object : challengeAdapter.onitemclick {
                        override fun itemClickListener(position: Int) {
                            Log.d("75", "itemClickListener: $position")
                            view =
                                View.inflate(this@challengeAct, R.layout.challenge_preview, null)
                            bottomDialog.setContentView(view)
                            bottomDialog.show()
                            val img = view.findViewById<ImageView>(R.id.prog_pic)
                            storageReference =
                                FirebaseStorage.getInstance().reference.child("Challenge/" + auth.currentUser?.uid)
                                    .child("Day$position")
                            storageReference.downloadUrl.addOnSuccessListener { uri ->
                                Glide.with(view).load(uri).into(img)
                            }.addOnFailureListener { exception -> }

                            val t1 = view.findViewById<CheckBox>(R.id.task_1)
                            val t2 = view.findViewById<CheckBox>(R.id.task_2)
                            val t3 = view.findViewById<CheckBox>(R.id.task_3)
                            val t4 = view.findViewById<CheckBox>(R.id.task_4)
                            val t5 = view.findViewById<CheckBox>(R.id.task_5)
                            val learning = view.findViewById<TextView>(R.id.learning)
                            t1.isChecked = list[position].Diet.toBoolean()
                            t1.isClickable = false
                            t2.isChecked = list[position].Workout.toBoolean()
                            t2.isClickable = false
                            t3.isChecked = list[position].Water.toBoolean()
                            t3.isClickable = false
                            t4.isChecked = list[position].Reading.toBoolean()
                            t4.isClickable = false
                            t5.isChecked = list[position].ProgressPic.toBoolean()
                            t5.isClickable = false
                            learning.text = list[position].Learning
//                            learning.hint = list[position].learning.toString()
                            Log.d("75", "itemClickListener: ${list}")

                        }

                    })
                }

            }
    }

    fun streakdata(streak: Int = 0, sts: Boolean = false) {
        val streakEntry = hashMapOf(
            "Streak" to streak,
            "Status" to sts
        )
        fsStore.collection("Streak").document(auth.currentUser?.uid!!)
            .collection("mystreak").document(auth.currentUser?.uid!! + "Streak").get()
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    val result = it.result
                    if (result != null && result.exists()) {
                        fsStore.collection("Streak").document(auth.currentUser?.uid!!)
                            .collection("mystreak").document(auth.currentUser?.uid!! + "Streak")
                            .set(streakEntry).addOnCompleteListener {
                                Log.d("75", "streakdata: Updated")
                            }
                    } else {
                        val intStreakEntry = hashMapOf(
                            "Streak" to 0,
                            "Status" to false
                        )
                        fsStore.collection("Streak").document(auth.currentUser?.uid!!)
                            .collection("mystreak").document(auth.currentUser?.uid!! + "Streak")
                            .set(intStreakEntry).addOnCompleteListener {
                                Log.d("75", "streakdata: Done")
                            }
                    }
                }

            }
    }
}
