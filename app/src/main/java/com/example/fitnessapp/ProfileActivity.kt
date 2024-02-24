package com.example.fitnessapp

import android.Manifest
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.fitnessapp.databinding.ActivityProfileBinding
import com.example.fitnessapp.databinding.CustomProgressBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var DataList: ArrayList<Menu>
    private lateinit var bindProfile: ActivityProfileBinding
    private lateinit var storageReference: StorageReference
    private lateinit var db: DatabaseReference
    private lateinit var fsStore: FirebaseFirestore
    private lateinit var sharedPre: SharedPreferences
    private lateinit var Prefeditor: SharedPreferences.Editor
    private var reminder: Boolean = false

    var BmiValue = ""
    var HeightValue = ""
    var WeightValue = ""
    private val PREFS_NAME = "StreakPrefs"
    private val STREAK_KEY = "streak"
    private val shared_Preferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
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
//                val loading = custom_prog(this)
//                loading.start()
                val dialog = Dialog(this)
                val layout = CustomProgressBinding.inflate(layoutInflater)
                dialog.setContentView(layout.root)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                storageReference =
                    FirebaseStorage.getInstance().getReference("Users/" + auth.currentUser?.uid)
                storageReference.putFile(result.data?.data!!).addOnSuccessListener {
                    Log.d("hello", "onCreate: Uploaded")
                    galleryprofile()
//                    loading.dismiss()
                    dialog.dismiss()
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
        fsStore = FirebaseFirestore.getInstance()
        check()
        sharedPreferences =
            applicationContext.getSharedPreferences("USERDATA", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        Log.d("hello", "onCreate: ${sharedPreferences.all}")
        emailAddress = sharedPreferences.getString("email", "").toString()
        username = sharedPreferences.getString("username", "").toString()
        bindProfile.email.text = emailAddress
        bindProfile.username.text = username
        editor.commit()

        sharedPre = applicationContext.getSharedPreferences("Notification", MODE_PRIVATE)
        Prefeditor = sharedPreferences.edit()
        Prefeditor.commit()
        val sts = sharedPre.getBoolean("Reminder", false)
        Log.d("75", "onCreate: $reminder")

        reminderCheck()

        bindProfile.linearLayout4.setOnLongClickListener {
            val clipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val username = bindProfile.username.text
            val email = bindProfile.email.text
            val data = "${username}\n" + email
            val clipData = ClipData.newPlainText("text", data)
            clipboardManager.setPrimaryClip(clipData)
            true
        }

        bindProfile.profilePic.setOnLongClickListener {
            val resources = resources
            val drawableId =
               R.drawable.profile// Replace with your actual drawable resource ID

            val uri = Uri.parse(
                "${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                        "${resources.getResourcePackageName(drawableId)}/" +
                        "${resources.getResourceTypeName(drawableId)}/" +
                        resources.getResourceEntryName(drawableId)
            )

            MaterialAlertDialogBuilder(
                this@ProfileActivity,
                R.style.ThemeOverlay_App_MaterialAlertDialog
            )
                .setTitle("Remove Profile Pic")
                .setIcon(R.drawable.appicon)
                .setMessage("Are you sure you want to remove Profile pic?")
                .setPositiveButton("Yes") { dialog, which ->
                    storageReference =
                        FirebaseStorage.getInstance().getReference("Users/" + auth.currentUser?.uid)
                    storageReference.putFile(uri).addOnSuccessListener {
                        Log.d("hello", "onCreate: Default Uploaded")
                        galleryprofile()
                        dialog.dismiss()
//                    loading.dismiss()
                    }.addOnFailureListener {
                        Log.d("hello", "onCreate: Failed")

                    }
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .show();

            true

        }

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
            arrayOf(
                "RECORDS",
                "75 HARD CHALLENGE",
                "EDIT PROFILE PIC",
                "RESET PASSWORD",
                "SUPPORT",
                "LOGOUT"
            )
        val IconID: Array<Int> = arrayOf(
            R.drawable.baseline_history_24,
            R.drawable.baseline_fitness_center_24,
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
                    2 -> {
                        if (user != null) {
                            Log.d("hello", "onCreate: ${user}")
                            requestpermission()
                            profileImage()
                        }
                    }

                    1 -> {
                        streakdata()
                    }

                    0 -> {
                        intent = Intent(this@ProfileActivity, HistoryActivity::class.java)
                        startActivity(intent)
                    }

                    3 -> {
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
//                                        openEmailApp()
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
                        val linearLayout = bindProfile.profilemain

                        // Convert the LinearLayout to a bitmap
                        val bitmap = viewToBitmap(linearLayout)

                        // Share the bitmap
                        shareBitmap(this@ProfileActivity, bitmap)
//                        initialChallenge()
                    }

                    5 -> {
                        MaterialAlertDialogBuilder(
                            this@ProfileActivity,
                            R.style.ThemeOverlay_App_MaterialAlertDialog
                        )
                            .setTitle("Log Out")
                            .setIcon(R.drawable.logout)
                            .setMessage("Are you sure you want to log out?")
                            .setPositiveButton("Yes") { dialog, which ->
                                editor.clear()
                                editor.commit()
                                shared_Preferences.edit().putInt(STREAK_KEY, 0).apply()
                                auth.signOut()
                                intent = Intent(this@ProfileActivity, WelcomeActivity::class.java)
                                startActivity(intent)
                            }
                            .setNegativeButton("No") { dialog, which ->
                                dialog.dismiss()
                            }
                            .show();
//                        editor.clear()
//                        editor.commit()
//                        shared_Preferences.edit().putInt(STREAK_KEY, 0).apply()
//                        auth.signOut()
//                        intent = Intent(this@ProfileActivity, WelcomeActivity::class.java)
//                        startActivity(intent)

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


    fun streakdata(streak: Int = 0, sts: Boolean = false) {
        val streakEntry = hashMapOf(
            "Streak" to streak,
            "Status" to sts,
        )
        fsStore.collection("Streak").document(auth.currentUser?.uid!!)
            .collection("mystreak").document(auth.currentUser?.uid!! + "Streak")
            .get()
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    val result = it.result
                    val getstreak = result.getField<Int>("Streak")
                    val remindersts = result.getField<Boolean>("Reminder")
                    if (remindersts != null) {
                        reminder = remindersts
                    }
                    Log.d("75", "check__: $remindersts")
                    if (result != null && result.exists() && getstreak != 0) {
                        intent = Intent(this@ProfileActivity, challengeAct::class.java)
                        startActivity(intent)
                    } else {
                        MaterialAlertDialogBuilder(this,R.style.ThemeOverlay_App_MaterialAlertDialog)
                            .setTitle("Start 75 Hard Challenge")
                            .setMessage("Are you ready to take on the 75 Hard Challenge? This challenge involves committing to a set of daily tasks for 75 consecutive days.")
                            .setPositiveButton("Yes") { dialog, which ->
                                intent = Intent(this@ProfileActivity, challengeAct::class.java)
                                startActivity(intent)
                                dialog.dismiss()

                                val intStreakEntry = hashMapOf(
                                    "Streak" to 0,
                                    "Status" to false,
                                    "Reminder" to false //debug
                                )
                                fsStore.collection("Streak").document(auth.currentUser?.uid!!)
                                    .collection("mystreak")
                                    .document(auth.currentUser?.uid!! + "Streak")
                                    .set(intStreakEntry).addOnCompleteListener {
                                        Log.d("75", "streakdata: Done")
                                    }
                            }
                            .setNegativeButton("Cancel") { dialog, which ->
                                dialog.dismiss()
                                Log.d("Btn", "No Clicked")

                            }
                            .show()
//                        val dialog = Dialog(this@ProfileActivity)
//                        val layout = ConfirmationDialogBinding.inflate(layoutInflater)
//                        dialog.setContentView(layout.root)
//                        dialog.setCanceledOnTouchOutside(false)
//                        dialog.show()
//                        layout.no.setOnClickListener {
//                            dialog.dismiss()
//                            Log.d("Btn", "No Clicked")
//                        }
//                        layout.yes.setOnClickListener {
//                            intent = Intent(this@ProfileActivity, challengeAct::class.java)
//                            startActivity(intent)
//                            dialog.dismiss()
//
//
//                            val intStreakEntry = hashMapOf(
//                                "Streak" to 0,
//                                "Status" to false
//                            )
//                            fsStore.collection("Streak").document(auth.currentUser?.uid!!)
//                                .collection("mystreak").document(auth.currentUser?.uid!! + "Streak")
//                                .set(intStreakEntry).addOnCompleteListener {
//                                    Log.d("75", "streakdata: Done")
//                                }
//                        }

                    }
                }

            }
    }

    fun check() {

        fsStore.collection("Streak")
            .document(auth.currentUser?.uid!!)
            .collection("mystreak")
            .document(auth.currentUser?.uid!! + "Streak")
            .get().addOnSuccessListener { it ->
                if (it.exists()) {
                    val getstreak = it.getField<Int>("Streak")
                    Log.d("75", "check: $getstreak")
                    if (getstreak != 0) {
                        val date =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        fsStore.collection("Challenge").document(auth.currentUser?.uid!!)
                            .collection("mychallenge")
                            .whereEqualTo("Date", date).get().addOnSuccessListener { result ->
                                if (result.isEmpty) {
                                    val dateFormat =
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val currentDate = Date()
                                    val calendar = Calendar.getInstance()
                                    calendar.time = currentDate
                                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                                    val yesterdayDate = calendar.time
                                    val yesterdayFormattedDate = dateFormat.format(yesterdayDate)
                                    fsStore.collection("Challenge")
                                        .document(auth.currentUser?.uid!!)
                                        .collection("mychallenge")
                                        .whereEqualTo("Date", yesterdayFormattedDate).get()
                                        .addOnSuccessListener { yesterday ->
                                            if (yesterday.isEmpty) {
                                                // hello world
                                                fsStore.collection("Challenge")
                                                    .document(auth.currentUser?.uid!!)
                                                    .collection("mychallenge").get()
                                                    .addOnSuccessListener { it ->
                                                        for (document in it.documents) {
                                                            document.reference.delete()
                                                                .addOnSuccessListener {
                                                                    Log.d(
                                                                        "75",
                                                                        "Document successfully deleted!"
                                                                    )
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Log.w(
                                                                        "75",
                                                                        "Error deleting document",
                                                                        e
                                                                    )
                                                                }
                                                        }
                                                    }

                                                val intStreakEntry = hashMapOf(
                                                    "Streak" to 0,
                                                    "Status" to false,
                                                    "Reminder" to false
                                                )
                                                fsStore.collection("Streak")
                                                    .document(auth.currentUser?.uid!!)
                                                    .collection("mystreak")
                                                    .document(auth.currentUser?.uid!! + "Streak")
                                                    .set(intStreakEntry).addOnCompleteListener {
                                                        Log.d("75", "streakdata: Done")
                                                    }

                                                Log.d("75", "Streak Broken")
                                            } else {
                                                Log.d("75", "Streak hello")
                                            }
                                            Log.d("75", "uploadToStore: Done")
                                        }
                                } else {
                                    Log.d("75", "Fitness activity for today already logged")
                                }
                            }.addOnFailureListener {
                                Log.w("75", "Error checking for existing fitness record")
                            }

                    } else {
                        bindProfile.streakContainer.visibility = View.INVISIBLE
                        Log.d("75", "check: Start new challenge")
                    }

                    if (getstreak != 0) {
                        bindProfile.streakContainer.visibility = View.VISIBLE
                        bindProfile.streakTxt.text = getstreak.toString()
                    }
                }

            }
    }

    override fun onResume() {
        super.onResume()
        profile()
        check()
        reminderCheck()
    }


    fun initialChallenge() {
        MaterialAlertDialogBuilder(this,R.style.ThemeOverlay_App_MaterialAlertDialog).setTitle(R.string.app_name).setMessage("hello world")
            .show()
    }

    private fun openEmailApp() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:") // Only email apps should handle this

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Log.d("profile", "openEmailApp: ")
            // If no email app is available, you can handle this case accordingly
            // For example, show a message to the user
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        val pm = packageManager
        val intent = pm.getLaunchIntentForPackage(packageName)
        return intent != null
    }

    private fun reminderCheck() {
        fsStore.collection("Streak").document(auth.currentUser?.uid!!)
            .collection("mystreak").document(auth.currentUser?.uid!! + "Streak")
            .get().addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    val result = it.result
                    val reminderVal = result.getField<Boolean>("Reminder")
                    if (reminderVal != null) {
                        reminder = reminderVal
                    }
                    if (reminderVal == true) {
                        bindProfile.remainderContainer.visibility = View.VISIBLE
                    } else {
                        bindProfile.remainderContainer.visibility = View.GONE
                    }
                    Log.d("75", "onCreate Act: $reminderVal")
                }
            }.addOnFailureListener {
                Log.d("75", "onCreate: Failed")
            }
    }

    fun viewToBitmap(view: View): Bitmap {
        // Create a bitmap with the same dimensions as the view
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        // Create a canvas with the bitmap
        val canvas = Canvas(bitmap)
        // Draw the view onto the canvas
        view.draw(canvas)
        return bitmap
    }

    // Function to share a bitmap image with other apps without saving
    fun shareBitmap(context: Context, bitmap: Bitmap) {
        // Convert bitmap to byte array
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)

        try {
            // Create a temporary file
            val file = File(context.externalCacheDir, "shared_image.png")
            val fos = FileOutputStream(file)
            fos.write(bytes.toByteArray())
            fos.flush()
            fos.close()

            // Get the URI of the temporary file
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

            // Create a share intent
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Launch the sharing activity
            context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}