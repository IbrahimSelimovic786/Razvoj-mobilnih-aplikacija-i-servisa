package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.familyhustle.databinding.ActivityCreateTaskBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CreateTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var database: DatabaseReference
    private val houseList = mutableListOf<String>()
    private val houseMap = mutableMapOf<String, String>()

    private fun getUserRoleInHouse(houseId: String, userEmail: String, callback: (String) -> Unit) {
        val encodedEmail = encodeEmail(userEmail) // Kodirajte email ako koristite email kao ključ
        val houseRef = FirebaseDatabase.getInstance().getReference("houses")
            .child(houseId)
            .child("members")
            .child(encodedEmail)

        houseRef.get().addOnSuccessListener { snapshot ->
            val role = snapshot.getValue(String::class.java) ?: "Member"
            callback(role)
        }.addOnFailureListener {
            callback("Member")
        }
    }


    private fun encodeEmail(email: String): String {
        return email.replace(".", "_")
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        loadHouses()

        binding.btnSaveTask.setOnClickListener {
            val selectedHouseName = binding.spinnerHouse.selectedItem.toString()
            val selectedHouseId = houseMap[selectedHouseName] // Dobijamo houseId iz mape

            if (selectedHouseId == null) {
                Toast.makeText(this, "House ID not found!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getUserRoleInHouse(selectedHouseId, FirebaseAuth.getInstance().currentUser?.email ?: "") { role ->
                if (role != "Parent") {
                    Toast.makeText(this, "Only parents can create tasks!", Toast.LENGTH_SHORT).show()
                    return@getUserRoleInHouse
                }

                val taskName = binding.etTaskName.text.toString()
                val dueDate = getDueDate() // Kombinovanje unosa za godinu, mjesec i dan
                val taskDescription = binding.etTaskDescription.text.toString()

                if (taskName.isNotEmpty() && dueDate.isNotEmpty() && taskDescription.isNotEmpty()) {
                    val weekNumber = getWeekNumber(dueDate)
                    if (weekNumber == null) {
                        Toast.makeText(this, "Invalid date. Please ensure it's a valid date in yyyy-MM-dd format.", Toast.LENGTH_SHORT).show()
                    } else {
                        saveTaskToDatabase(selectedHouseId, taskName, taskDescription, dueDate, weekNumber)
                    }
                } else {
                    Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                }
            }
        }







        binding.btnCreateHouse.setOnClickListener {
            startActivity(Intent(this, CreateHouseActivity::class.java))
        }

        setupBottomNavigation()
    }

    private fun loadHouses() {
        val houseRef = FirebaseDatabase.getInstance().getReference("houses")
        houseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val houseNames = mutableListOf<String>()
                houseMap.clear()

                for (houseSnapshot in snapshot.children) {
                    val houseId = houseSnapshot.key ?: continue
                    val houseName = houseSnapshot.child("name").getValue(String::class.java) ?: continue

                    houseNames.add(houseName)
                    houseMap[houseName] = houseId // Spremamo naziv kuće i houseId
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

    private fun getDueDate(): String {
        val year = binding.etYear.text.toString()
        val month = binding.etMonth.text.toString().padStart(2, '0') // Dodaje '0' ako je jednocifreno
        val day = binding.etDay.text.toString().padStart(2, '0')

        return if (year.isNotEmpty() && month.isNotEmpty() && day.isNotEmpty()) {
            "$year-$month-$day"
        } else {
            ""
        }
    }
    private fun saveTaskToDatabase(houseName: String, taskName: String, description: String, dueDate: String, weekNumber: Int) {
        val taskRef = database.child("tasks").push()

        // Provjera unosa bodova
        val taskPoints = binding.etTaskPoints.text.toString().toIntOrNull()
        if (taskPoints == null || taskPoints <= 0) {
            Toast.makeText(this, "Please enter valid points for the task!", Toast.LENGTH_SHORT).show()
            return
        }

        val task = TaskData(
            id = taskRef.key ?: "",
            name = taskName,
            description = description,
            dueDate = dueDate,
            houseName = houseName,
            weekNumber = weekNumber,
            completed = false,
            points = taskPoints // Dodavanje unesenih bodova
        )
        taskRef.setValue(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task created successfully with $taskPoints points!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error saving task: ${error.message}", Toast.LENGTH_SHORT).show()
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
                R.id.nav_tasks -> {
                    // Navigacija na CreateTaskActivity
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
