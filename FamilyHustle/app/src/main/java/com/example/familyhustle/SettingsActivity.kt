package com.example.familyhustle

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.familyhustle.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserProfile()
        setupBottomNavigation()
        binding.ivProfile.setOnClickListener {
            selectImage()
        }

        binding.btnLogout.setOnClickListener {
            // Odjava iz Firebase Authentication-a
            FirebaseAuth.getInstance().signOut()

            // Očisti podatke o "Zapamti me" ako postoje
            val sharedPreferences = getSharedPreferences("FamilyHustlePrefs", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            // Pokreni LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Osigurava da nema povratka na prethodnu aktivnost
            startActivity(intent)

            // Prikazi poruku korisniku
            Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show()
        }




    }

    private fun loadUserProfile() {
        // Učitavanje korisničkog imena
        userRef.child("username").get().addOnSuccessListener { snapshot ->
            val username = snapshot.getValue(String::class.java) ?: "User"
            binding.etUsername.setText(username)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load username.", Toast.LENGTH_SHORT).show()
        }

        // Učitavanje slike profila
        userRef.child("profilePicture").get().addOnSuccessListener { snapshot ->
            val base64Image = snapshot.getValue(String::class.java)
            if (!base64Image.isNullOrEmpty()) {
                val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.ivProfile.setImageBitmap(bitmap)
            } else {
                binding.ivProfile.setImageResource(R.drawable.default_profile) // Default slika
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile picture.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val imageUri = data?.data
            val inputStream = contentResolver.openInputStream(imageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            binding.ivProfile.setImageBitmap(bitmap) // Prikaz slike lokalno
            saveProfilePicture(bitmap)
        }
    }

    private fun saveProfilePicture(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        userRef.child("profilePicture").setValue(base64Image)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_settings)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish() // Završite trenutnu aktivnost
                    true
                }
                R.id.nav_tasks -> {
                    startActivity(Intent(this, CreateTaskActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    // Već ste na SettingsActivity, ne radite ništa
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

}
