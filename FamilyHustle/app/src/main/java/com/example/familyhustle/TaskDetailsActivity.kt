package com.example.familyhustle

import android.Manifest
import android.view.View
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.familyhustle.databinding.ActivityTaskDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_IMAGE_CAPTURE = 1
    private var taskId: String? = null
    private var isPhotoTaken = false
    private var photoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()

        taskId = intent.getStringExtra("TASK_ID")
        var houseId = intent.getStringExtra("HOUSE_ID")
        val taskName = intent.getStringExtra("TASK_NAME")
        val taskDescription = intent.getStringExtra("TASK_DESCRIPTION")
        val taskDueDate = intent.getStringExtra("TASK_DUE_DATE")
        val taskPoints = intent.getIntExtra("TASK_POINTS",0) // Dodana linija za bodove


        Log.d("TaskDetailActivity", "Initial data - TASK_ID: $taskId, HOUSE_ID: $houseId")

        if (taskId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Missing task information", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fallback to retrieve house ID if not provided
        if (houseId.isNullOrEmpty()) {
            retrieveHouseIdFromTask(taskId!!) { retrievedHouseId ->
                houseId = retrievedHouseId
                setupTaskDetails(taskName, taskDescription, taskDueDate, houseId!!)
            }
        } else {
            setupTaskDetails(taskName, taskDescription, taskDueDate, houseId!!)
        }
    }

    private fun retrieveHouseIdFromTask(taskId: String, callback: (String) -> Unit) {
        FirebaseDatabase.getInstance().reference.child("tasks").child(taskId)
            .get()
            .addOnSuccessListener { snapshot ->
                val retrievedHouseId = snapshot.child("houseName").getValue(String::class.java)
                if (retrievedHouseId != null) {
                    callback(retrievedHouseId)
                } else {
                    Log.e("TaskDetailActivity", "Could not retrieve house ID from task")
                    Toast.makeText(this, "Error: Could not load task details", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Log.e("TaskDetailActivity", "Failed to retrieve task details")
                Toast.makeText(this, "Error: Failed to load task", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun setupTaskDetails(taskName: String?, taskDescription: String?, taskDueDate: String?, houseId: String) {
        binding.tvTaskName.text = taskName
        binding.tvTaskDescription.text = taskDescription
        binding.tvTaskDueDate.text = "Due: $taskDueDate"

        getCurrentUserRole(houseId) { role ->
            if (role == "Parent") {
                setupParentView()
                loadPhoto()
            } else if (role == "Member") {
                setupMemberView()
            }
        }
    }




    private fun setupParentView() {
        binding.btnTakePhoto.visibility = View.GONE
        binding.btnCompleteTask.visibility = View.GONE

        // Prikaži opcije za roditelja
        binding.btnApproveTask.visibility = View.VISIBLE
        binding.btnDoTask.visibility = View.VISIBLE
        binding.btnSeeDoneTask.visibility = View.VISIBLE

        // Sakrij sliku dok roditelj ne odabere opciju za pregled
        binding.imgTaskPhoto.visibility = View.GONE

        binding.btnDoTask.setOnClickListener {
            showMemberViewForParent()
        }

        binding.btnSeeDoneTask.setOnClickListener {
            showParentApprovalView()
        }

        binding.btnApproveTask.setOnClickListener {
            approveTask()
        }
    }


    private fun showMemberViewForParent() {
        // Reset vidljivosti
        binding.btnDoTask.visibility = View.GONE
        binding.btnSeeDoneTask.visibility = View.GONE
        binding.btnApproveTask.visibility = View.GONE

        // Prikaz član funkcionalnosti
        setupMemberView()
        Toast.makeText(this, "You can now complete the task!", Toast.LENGTH_SHORT).show()
    }

    private fun showParentApprovalView() {
        // Reset vidljivosti
        binding.btnDoTask.visibility = View.GONE
        binding.btnSeeDoneTask.visibility = View.GONE
        binding.btnTakePhoto.visibility = View.GONE
        binding.btnCompleteTask.visibility = View.GONE
        binding.imgTaskPhoto.visibility = View.VISIBLE

        // Prikaz opcije za odobravanje zadatka
        binding.btnApproveTask.visibility = View.VISIBLE
        loadPhoto() // Učitaj podatke za pregled
        Toast.makeText(this, "Review and approve the task.", Toast.LENGTH_SHORT).show()
    }




    //    private fun setupParentView() {
//        binding.btnTakePhoto.visibility = View.GONE
//        binding.btnCompleteTask.visibility = View.GONE
//        binding.btnApproveTask.visibility = View.VISIBLE
//
//        binding.btnApproveTask.setOnClickListener {
//            Log.d("TaskDetailActivity", "Approve clicked - photoUrl: $photoUrl, isPhotoTaken: $isPhotoTaken")
//
//            if (photoUrl.isNullOrEmpty()) {
//                // Double-check if photo exists in database
//                FirebaseDatabase.getInstance().reference.child("tasks").child(taskId!!)
//                    .child("photoBase64").get()
//                    .addOnSuccessListener { snapshot ->
//                        val base64String = snapshot.getValue(String::class.java)
//                        if (!base64String.isNullOrEmpty()) {
//                            // Photo exists in database, proceed with approval
//                            approveTask()
//                        } else {
//                            Toast.makeText(this, "No photo uploaded to approve!", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//            } else {
//                // Photo URL is not null, proceed with approval
//                approveTask()
//            }
//        }
//    }
    private fun approveTask() {
        val database = FirebaseDatabase.getInstance().reference

        database.child("tasks").child(taskId!!).get().addOnSuccessListener { taskSnapshot ->
            val taskPoints = taskSnapshot.child("points").getValue(Int::class.java) ?: 0
            val completedByUserId = taskSnapshot.child("completedBy").getValue(String::class.java)

            if (completedByUserId.isNullOrEmpty()) {
                Toast.makeText(this, "No user found who completed this task.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Dodaj bodove korisniku koji je završio zadatak
            database.child("users").child(completedByUserId).child("points").get()
                .addOnSuccessListener { userSnapshot ->
                    val currentPoints = userSnapshot.getValue(Int::class.java) ?: 0
                    val newPoints = currentPoints + taskPoints

                    database.child("users").child(completedByUserId).child("points").setValue(newPoints)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Task approved! $taskPoints points awarded to the user.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Failed to update points: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Failed to fetch user points: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { error ->
            Toast.makeText(this, "Failed to fetch task details: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }




    private fun setupMemberView() {
        binding.btnTakePhoto.visibility = View.VISIBLE
        binding.btnCompleteTask.visibility = View.VISIBLE

        // Sakrij opcije za roditelje
        binding.btnApproveTask.visibility = View.GONE
        binding.btnDoTask.visibility = View.GONE
        binding.btnSeeDoneTask.visibility = View.GONE

        // Prikaži sliku ako postoji
        binding.imgTaskPhoto.visibility = View.VISIBLE

        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }

        binding.btnCompleteTask.setOnClickListener {
            if (!isPhotoTaken) {
                Toast.makeText(this, "Please take a photo of the task first!", Toast.LENGTH_SHORT).show()
            } else {
                markTaskAsCompleted()
            }
        }
    }


    private fun takePhoto() {
        if (checkCameraPermission()) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(this, "Camera is not available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val photoBitmap = data?.extras?.get("data") as? Bitmap
            if (photoBitmap != null) {
                binding.imgTaskPhoto.setImageBitmap(photoBitmap) // Prikaz slike u ImageView
                isPhotoTaken = true // Oznaka da je slika uspješno snimljena
                savePhotoToFirebase(photoBitmap) // Spremanje slike u bazu
            } else {
                Toast.makeText(this, "Failed to capture photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun savePhotoToFirebase(photoBitmap: Bitmap) {
        // Compress and convert image to Base64
        val baos = ByteArrayOutputStream()
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val byteArray = baos.toByteArray()
        val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

        // Save Base64 string and set photoUrl
        val database = FirebaseDatabase.getInstance().reference.child("tasks").child(taskId!!)
        database.child("photoBase64").setValue(base64String)
            .addOnSuccessListener {
                photoUrl = base64String  // Set photoUrl here
                Log.d("TaskDetailActivity", "Photo saved successfully in Firebase Database")
                isPhotoTaken = true
                Toast.makeText(this, "Photo saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                Log.e("TaskDetailActivity", "Failed to save photo: ${error.message}")
                Toast.makeText(this, "Failed to save photo.", Toast.LENGTH_SHORT).show()
            }
    }







    private fun loadPhoto() {
        val database = FirebaseDatabase.getInstance().reference.child("tasks").child(taskId!!)
        database.child("photoBase64").get()
            .addOnSuccessListener { snapshot ->
                val base64String = snapshot.getValue(String::class.java)
                if (!base64String.isNullOrEmpty()) {
                    photoUrl = base64String  // Set photoUrl here
                    val byteArray = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    binding.imgTaskPhoto.setImageBitmap(bitmap)
                    isPhotoTaken = true  // Mark as photo taken
                } else {
                    photoUrl = null
                    isPhotoTaken = false
                    Toast.makeText(this, "No photo found for this task.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Log.e("TaskDetailActivity", "Failed to load photo: ${error.message}")
                photoUrl = null
                isPhotoTaken = false
                Toast.makeText(this, "Failed to load photo.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun checkCameraPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto()
            } else {
                Toast.makeText(this, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markTaskAsCompleted() {
        val database = FirebaseDatabase.getInstance().reference
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: User not authenticated!", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("tasks").child(taskId!!).get().addOnSuccessListener { taskSnapshot ->
            val taskPoints = taskSnapshot.child("points").getValue(Int::class.java) ?: 0

            database.child("tasks").child(taskId!!).child("completed").setValue(true)
                .addOnSuccessListener {
                    database.child("tasks").child(taskId!!).child("completedBy").setValue(currentUserId)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Task marked as completed. Awaiting approval.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Failed to save completion info: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Failed to mark task as completed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }



    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_tasks -> {
                    startActivity(Intent(this, CreateTaskActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_leaderboard -> {
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun getCurrentUserRole(houseId: String, callback: (String) -> Unit) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail == null) {
            Log.e("TaskDetailActivity", "User email is null")
            callback("Member") // Defaultna uloga ako je korisnik neautentifikovan
            return
        }

        // Zamjena "." sa "_"
        val formattedEmail = currentUserEmail.replace(".", "_")
        val database = FirebaseDatabase.getInstance().reference.child("houses").child(houseId).child("members").child(formattedEmail)

        database.get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.getValue(String::class.java) ?: "Member" // Ako uloga ne postoji, pretpostavlja se "Member"
                Log.d("TaskDetailActivity", "User role: $role")
                callback(role)
            }
            .addOnFailureListener { error ->
                Log.e("TaskDetailActivity", "Failed to fetch user role: ${error.message}")
                callback("Member") // U slučaju greške, pretpostavljamo Member
            }
    }



}
