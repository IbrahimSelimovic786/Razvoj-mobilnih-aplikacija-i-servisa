package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.familyhustle.databinding.ActivityLeaderboardBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth


class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var adapter: LeaderboardAdapter
    private val leaderboardData = mutableListOf<LeaderboardItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigation()

        // Postavljanje RecyclerView-a
        adapter = LeaderboardAdapter(leaderboardData)
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.rvLeaderboard.adapter = adapter

        // Učitavanje podataka iz Firebase-a
        loadLeaderboardData()
    }

    private fun loadLeaderboardData() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedEmail = encodeEmail(currentUserEmail)
        val database = FirebaseDatabase.getInstance().reference

        // Prvo pronađi kuću kojoj korisnik pripada
        database.child("houses").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var userHouseId: String? = null

                for (houseSnapshot in snapshot.children) {
                    val members = houseSnapshot.child("members")
                    if (members.hasChild(encodedEmail)) {
                        userHouseId = houseSnapshot.key
                        break
                    }
                }

                if (userHouseId == null) {
                    Toast.makeText(this@LeaderboardActivity, "No house found for your account.", Toast.LENGTH_SHORT).show()
                    return
                }

                // Učitaj samo članove te kuće za leaderboard
                loadLeaderboardForHouse(userHouseId)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LeaderboardActivity, "Error loading house: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadLeaderboardForHouse(houseId: String?) {
        if (houseId.isNullOrEmpty()) return

        val database = FirebaseDatabase.getInstance().reference
        val houseMembersRef = database.child("houses").child(houseId).child("members")
        val usersRef = database.child("users")

        houseMembersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val houseMembers = snapshot.children.map { it.key ?: "" }

                println("DEBUG: House members = $houseMembers")

                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnapshot: DataSnapshot) {
                        leaderboardData.clear()
                        for (encodedEmail in houseMembers) {
                            // Dekodiraj e-mail (zamjena `_` sa `.`)
                            val decodedEmail = encodedEmail.replace("_", ".")
                            val user = userSnapshot.children.firstOrNull {
                                it.child("email").getValue(String::class.java) == decodedEmail
                            }

                            if (user != null) {
                                val username = user.child("username").getValue(String::class.java) ?: "Unknown"
                                val points = user.child("points").getValue(Int::class.java) ?: 0
                                leaderboardData.add(LeaderboardItem(username, points))
                                println("DEBUG: Added user $username with $points points")
                            } else {
                                println("DEBUG: User with email $decodedEmail not found in users")
                            }
                        }
                        leaderboardData.sortByDescending { it.points }
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LeaderboardActivity, "Error loading users: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LeaderboardActivity, "Error loading house members: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }







    private fun encodeEmail(email: String): String {
        return email.replace(".", "_").lowercase() // Zamjena tačke s donjom crtom i mala slova
    }




    private fun setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_leaderboard)
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
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_leaderboard -> {
                    true
                }
                else -> false
            }
        }
    }
}

data class LeaderboardItem(val name: String, val points: Int)
