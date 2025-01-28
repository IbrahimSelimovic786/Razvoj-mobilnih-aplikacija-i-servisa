package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.familyhustle.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: DatabaseReference
    private val houses = mutableListOf<String>()
    private val houseMap = mutableMapOf<String, String>()
    private val weeks = listOf("Week 1", "Week 2", "Week 3", "Week 4")
    private val tasks = mutableListOf<TaskData>()
    private var selectedHouse: String? = null
    private var selectedWeek: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = TaskAdapter(mutableListOf())

        database = FirebaseDatabase.getInstance().reference
        loadProfilePicture()
        setupSpinner()
        setupWeeksRecyclerView()
        setupBottomNavigation()

        loadHouses()
        loadAllTasks()
    }

    private fun setupSpinner() {
        val houseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, houses)
        houseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerHouse.adapter = houseAdapter

        binding.spinnerHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedHouse = houses[position]
                Log.d("HomeActivity", "Selected house: $selectedHouse")
                filterTasks()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupWeeksRecyclerView() {
        binding.rvWeeks.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvWeeks.adapter = WeekAdapter(weeks) { selectedWeek ->
            this.selectedWeek = selectedWeek
            Log.d("HomeActivity", "Selected week: $selectedWeek")
            filterTasks()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_tasks -> {
                    startActivity(Intent(this, CreateTaskActivity::class.java))
                    true
                }
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

    private fun loadProfilePicture() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = database.child("users").child(userId).child("profilePicture")

        userRef.get().addOnSuccessListener { snapshot ->
            val base64Image = snapshot.getValue(String::class.java)
            if (!base64Image.isNullOrEmpty()) {
                val imageBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.ivProfile.setImageBitmap(bitmap)
            } else {
                binding.ivProfile.setImageResource(R.drawable.default_profile) // Default profilna slika
            }
        }.addOnFailureListener { error ->
            Toast.makeText(this, "Failed to load profile picture: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun encodeEmail(email: String): String {
        return email.replace(".", "_").lowercase() // Zamjena tačke s donjom crtom i mala slova
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
                houses.clear()
                houseMap.clear()

                for (houseSnapshot in snapshot.children) {
                    val houseId = houseSnapshot.key ?: continue
                    val members = houseSnapshot.child("members")

                    // Dodaj samo kuće gdje trenutni korisnik postoji u members čvoru
                    if (members.hasChild(encodedEmail)) {
                        val houseName = houseSnapshot.child("name").getValue(String::class.java) ?: continue
                        houses.add(houseName)
                        houseMap[houseName] = houseId
                    }
                }

                if (houses.isEmpty()) {
                    Toast.makeText(this@HomeActivity, "No houses found for your account.", Toast.LENGTH_SHORT).show()
                }

                (binding.spinnerHouse.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load houses: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadAllTasks() {
        val taskRef = database.child("tasks")
        taskRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tasks.clear()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(TaskData::class.java)
                    if (task != null) {
                        tasks.add(task)
                    }
                }
                Log.d("HomeActivity", "All tasks loaded: $tasks")
                filterTasks()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load tasks: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterTasks() {
        if (selectedHouse == null || selectedWeek == null) {
            Log.d("HomeActivity", "House or Week not selected, clearing tasks.")
            (binding.rvTasks.adapter as TaskAdapter).updateTasks(emptyList())
            return
        }

        val houseId = houseMap[selectedHouse]
        val weekIndex = weeks.indexOf(selectedWeek) + 1
        if (houseId == null || weekIndex == 0) {
            Log.d("HomeActivity", "Invalid house or week selection.")
            (binding.rvTasks.adapter as TaskAdapter).updateTasks(emptyList())
            return
        }

        val filteredTasks = tasks.filter { task ->
            task.houseName == houseId && task.weekNumber == weekIndex
        }

        Log.d("HomeActivity", "Filtered tasks: $filteredTasks")
        (binding.rvTasks.adapter as TaskAdapter).updateTasks(filteredTasks)
    }


}