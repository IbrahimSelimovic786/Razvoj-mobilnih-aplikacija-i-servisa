package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.familyhustle.databinding.ActivityCreateHouseBinding
import com.google.firebase.database.FirebaseDatabase

class CreateHouseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateHouseBinding
    private val members = mutableListOf<Member>() // Lista članova

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateHouseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Postavljanje spinnera za uloge
        val roles = listOf("Parent", "Member")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = roleAdapter

        binding.btnAddMember.setOnClickListener {
            val email = binding.etMemberEmail.text.toString()
            val role = binding.spinnerRole.selectedItem.toString()

            if (email.isNotEmpty()) {
                members.add(Member(email, role))
                updateMemberList()
                binding.etMemberEmail.text.clear()
            } else {
                Toast.makeText(this, "Please enter a member email!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveHouse.setOnClickListener {
            val houseName = binding.etHouseName.text.toString()

            if (houseName.isNotEmpty() && members.isNotEmpty()) {
                saveHouseToDatabase(houseName)
            } else {
                Toast.makeText(this, "Please enter house name and add at least one member!", Toast.LENGTH_SHORT).show()
            }
        }

        setupBottomNavigation()
    }

    private fun encodeEmail(email: String): String {
        return email.replace(".", "_")
    }

    private fun updateMemberList() {
        // Ažurira TextView s listom članova
        binding.tvMembers.text = members.joinToString("\n") { "${it.name} - ${it.role}" }
    }

    private fun saveHouseToDatabase(houseName: String) {
        val database = FirebaseDatabase.getInstance().getReference("houses")
        val houseId = database.push().key // Generiše jedinstveni ID za kuću

        if (houseId != null) {
            // Koristite email kao ključ u Firebase strukturi
            val memberMap = members.associate { encodeEmail(it.name) to it.role }
            val house = mapOf(
                "id" to houseId,
                "name" to houseName,
                "members" to memberMap
            )
            database.child(houseId).setValue(house)
                .addOnSuccessListener {
                    Toast.makeText(this, "House $houseName saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Error saving house: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Error generating house ID!", Toast.LENGTH_SHORT).show()
        }
    }





    private fun setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_tasks)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_tasks -> {
                    startActivity(Intent(this, CreateTaskActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    // Navigacija na SettingsActivity
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_leaderboard -> {
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}

// Model za člana kuće
data class Member(
    val name: String,
    val role: String
)

// Model za kuću
data class House(
    val id: String,
    val name: String,
    val members: List<Member>
)
