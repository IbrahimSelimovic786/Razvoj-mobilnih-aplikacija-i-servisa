package com.example.familyhustle

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {

    private lateinit var rvWeekHistory: RecyclerView
    private lateinit var rvTasks: RecyclerView
    private lateinit var weekAdapter: WeekAdapter
    private lateinit var taskAdapter: TaskAdapter
    private val weekList = mutableListOf<Week>()
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // RecyclerView za istoriju sedmica
        rvWeekHistory = findViewById(R.id.rvWeekHistory)
        rvWeekHistory.layoutManager = LinearLayoutManager(this)
        weekAdapter = WeekAdapter(weekList) { week ->
            Toast.makeText(this, "Kliknuto: ${week.title}", Toast.LENGTH_SHORT).show()
        }
        rvWeekHistory.adapter = weekAdapter
        val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
        val allEntries = sharedPref.all

        for ((key, value) in allEntries) {
            Log.d("SharedPreferences", "$key: $value")
        }

        // RecyclerView za zadatke
        rvTasks = findViewById(R.id.rvTasks)
        rvTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(taskList)
        rvTasks.adapter = taskAdapter

        // Učitavanje podataka
        loadWeekHistory()
        loadTasks()
    }

    private fun loadWeekHistory() {
        weekList.add(Week("Trenutna sedmica", "11 Nov - 18 Nov"))
        weekList.add(Week("Prošla sedmica", "4 Nov - 10 Nov"))
        weekList.add(Week("2 Weeks ago", "2 Nov - 09 Nov"))
        weekAdapter.notifyDataSetChanged()
    }

    private fun loadTasks() {
        taskList.add(Task("Usisavanje", "50 bodova", "Završiti do vecere"))
        taskList.add(Task("Brisanje prašine", "30 bodova", "Završiti prije 05"))
        taskList.add(Task("Ocistiti garazu", "25 bodova", "Završiti prije 08"))
        taskAdapter.notifyDataSetChanged()
    }
}


