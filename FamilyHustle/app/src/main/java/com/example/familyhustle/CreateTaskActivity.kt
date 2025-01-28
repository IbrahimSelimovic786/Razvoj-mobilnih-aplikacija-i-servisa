package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.familyhustle.databinding.ActivityCreateTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CreateTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var database: DatabaseReference
    private val houseMap = mutableMapOf<String, String>()
    private var userRole: String = "Member" // Zadana vrijednost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        loadHouses()

        binding.spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedHouseName = binding.spinnerHouse.selectedItem.toString()
                val selectedHouseId = houseMap[selectedHouseName]
                if (selectedHouseId != null) {
                    verifyUserMembership(selectedHouseId) // Nova funkcija za provjeru članstva
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnSaveTask.setOnClickListener {
            if (userRole != "Parent") {
                Toast.makeText(this, "Only parents can create tasks!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedHouseId = houseMap[binding.spinnerHouse.selectedItem.toString()]
            if (selectedHouseId == null) {
                Toast.makeText(this, "Please select a house!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val taskName = binding.etTaskName.text.toString()
            val dueDate = getDueDate()
            val taskDescription = binding.etTaskDescription.text.toString()
            val taskPoints = binding.etTaskPoints.text.toString().toIntOrNull()

            if (taskName.isNotEmpty() && dueDate.isNotEmpty() && taskDescription.isNotEmpty() && taskPoints != null && taskPoints > 0) {
                saveTaskToDatabase(selectedHouseId, taskName, taskDescription, dueDate, taskPoints)
            } else {
                Toast.makeText(this, "Please fill in all fields correctly!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCreateHouse.setOnClickListener {
            startActivity(Intent(this, CreateHouseActivity::class.java))
        }

        setupBottomNavigation()
    }

    private fun verifyUserMembership(houseId: String) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedEmail = encodeEmail(currentUserEmail)
        val membersRef = database.child("houses").child(houseId).child("members")

        membersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@CreateTaskActivity, "House members not found.", Toast.LENGTH_SHORT).show()
                    binding.btnSaveTask.isEnabled = false
                    return
                }

                // Dohvati sve članove kuće
                val membersMap = snapshot.value as? Map<String, String>
                println("DEBUG: Members map = $membersMap")
                println("DEBUG: Encoded email = $encodedEmail")

                if (membersMap != null && membersMap.containsKey(encodedEmail)) {
                    userRole = membersMap[encodedEmail] ?: "Member"
                    println("DEBUG: User role = $userRole") // Log za debug

                    if (userRole == "Parent") {
                        enableTaskCreation()
                    } else {
                        Toast.makeText(this@CreateTaskActivity, "You do not have permission to create tasks.", Toast.LENGTH_SHORT).show()
                        binding.btnSaveTask.isEnabled = false
                    }
                } else {
                    // Ako e-mail nije pronađen
                    println("DEBUG: User $encodedEmail not found in members map")
                    Toast.makeText(this@CreateTaskActivity, "Your account is not part of this house.", Toast.LENGTH_SHORT).show()
                    binding.btnSaveTask.isEnabled = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CreateTaskActivity, "Failed to load members: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun encodeEmail(email: String): String {
        return email.replace(".", "_").lowercase() // Zamjena tačke s donjom crtom i mala slova
    }



    private fun enableTaskCreation() {
        binding.btnSaveTask.isEnabled = true // Omogućuje dugme
        Toast.makeText(this, "You have permission to create tasks!", Toast.LENGTH_SHORT).show()
    }

    private fun loadHouses() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedEmail = encodeEmail(currentUserEmail)
        val houseRef = database.child("houses")

        houseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val houseNames = mutableListOf<String>()
                houseMap.clear()

                for (houseSnapshot in snapshot.children) {
                    val houseId = houseSnapshot.key ?: continue
                    val members = houseSnapshot.child("members")

                    // Provjera da li je korisnik član kuće
                    if (members.hasChild(encodedEmail)) {
                        val houseName = houseSnapshot.child("name").getValue(String::class.java) ?: continue
                        houseNames.add(houseName)
                        houseMap[houseName] = houseId
                    }
                }

                if (houseNames.isEmpty()) {
                    Toast.makeText(this@CreateTaskActivity, "No houses found for your account.", Toast.LENGTH_SHORT).show()
                }

                val adapter = ArrayAdapter(this@CreateTaskActivity, android.R.layout.simple_spinner_item, houseNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerHouse.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CreateTaskActivity, "Failed to load houses: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun saveTaskToDatabase(houseId: String, taskName: String, description: String, dueDate: String, points: Int) {
        val taskRef = database.child("tasks").push()
        val task = TaskData(
            id = taskRef.key ?: "",
            name = taskName,
            description = description,
            dueDate = dueDate,
            houseName = houseId,
            weekNumber = getWeekNumber(dueDate) ?: 0,
            completed = false,
            points = points
        )
        taskRef.setValue(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to create task: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getDueDate(): String {
        val year = binding.etYear.text.toString()
        val month = binding.etMonth.text.toString().padStart(2, '0')
        val day = binding.etDay.text.toString().padStart(2, '0')
        return if (year.isNotEmpty() && month.isNotEmpty() && day.isNotEmpty()) {
            "$year-$month-$day"
        } else {
            ""
        }
    }

    private fun getWeekNumber(date: String): Int? {
        return try {
            val day = date.split("-")[2].toInt()
            when (day) {
                in 1..7 -> 1
                in 8..14 -> 2
                in 15..21 -> 3
                in 22..31 -> 4
                else -> null
            }
        } catch (e: Exception) {
            null
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
                R.id.nav_tasks -> true
                R.id.nav_settings -> {
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
