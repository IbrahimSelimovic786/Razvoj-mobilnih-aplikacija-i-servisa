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
        val database = FirebaseDatabase.getInstance().reference.child("users")

        database.orderByChild("points").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                leaderboardData.clear()
                for (child in snapshot.children) {
                    val username = child.child("username").getValue(String::class.java) ?: "Unknown" // Koristi `username`
                    val points = child.child("points").getValue(Int::class.java) ?: 0
                    leaderboardData.add(LeaderboardItem(username, points))
                }
                leaderboardData.sortByDescending { it.points } // Sortiraj prema bodovima
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LeaderboardActivity, "Error loading leaderboard: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
