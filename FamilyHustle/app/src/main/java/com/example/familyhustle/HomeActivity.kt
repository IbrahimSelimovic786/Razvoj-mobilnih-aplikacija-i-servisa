package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var rvWeekHistory: RecyclerView
    private lateinit var rvTasks: RecyclerView
    private lateinit var weekAdapter: WeekAdapter
    private lateinit var taskAdapter: TaskAdapter
    private val weekList = mutableListOf<Week>()
    private val taskList = mutableListOf<Task>()
    private val taskMap = mutableMapOf<Int, MutableList<Task>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Navigacija na kalendar
        findViewById<ImageView>(R.id.ic_calendar).setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            startActivity(intent)
        }

        rvWeekHistory = findViewById(R.id.rvWeekHistory)
        rvWeekHistory.layoutManager = LinearLayoutManager(this)
        weekAdapter = WeekAdapter(weekList) { week ->
            val weekIndex = weekList.indexOf(week) + 1
            Log.d("HomeActivity", "Kliknuto na sedmicu: ${week.title}, indeks: $weekIndex")
            showTasksForWeek(weekIndex)
        }
        rvWeekHistory.adapter = weekAdapter

        rvTasks = findViewById(R.id.rvTasks)
        rvTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(taskList)
        rvTasks.adapter = taskAdapter

        loadWeekHistory()
        loadTasks()
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun loadWeekHistory() {
        weekList.clear()
        weekList.add(Week("Prva sedmica", "01 Dec - 07 Dec"))
        weekList.add(Week("Druga sedmica", "08 Dec - 14 Dec"))
        weekList.add(Week("Treća sedmica", "15 Dec - 21 Dec"))
        weekList.add(Week("Četvrta sedmica", "22 Dec - 31 Dec"))
        weekAdapter.notifyDataSetChanged()
    }

    private fun loadTasks() {
        val sharedPref = getSharedPreferences("TasksPref", MODE_PRIVATE)
        val allTasks = sharedPref.all

        taskList.clear()
        taskMap.clear()
        val calendar = Calendar.getInstance()

        for ((_, value) in allTasks) {
            val taskData = value.toString().split("|")
            if (taskData.size < 4) {
                Log.e("HomeActivity", "Neispravan format zadatka: $value")
                continue
            }

            val title = taskData[0]
            val points = taskData[1]
            val deadline = taskData[2]

            val deadlineDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(deadline)
            if (deadlineDate != null) {
                calendar.time = deadlineDate
                val taskWeek = calendar.get(Calendar.WEEK_OF_MONTH)

                if (!taskMap.containsKey(taskWeek)) {
                    taskMap[taskWeek] = mutableListOf()
                }
                taskMap[taskWeek]?.add(Task(title, points, deadline))
            }
        }

        for ((week, tasks) in taskMap) {
            Log.d("HomeActivity", "Sedmica $week: ${tasks.map { it.title }}")
        }

        val currentWeek = calendar.get(Calendar.WEEK_OF_MONTH)
        showTasksForWeek(currentWeek)
    }

    private fun showTasksForWeek(week: Int) {
        Log.d("HomeActivity", "Prikaz zadataka za sedmicu: $week")
        val tasksForSelectedWeek = taskMap[week] ?: mutableListOf()

        taskList.clear()
        taskList.addAll(tasksForSelectedWeek)
        taskAdapter.notifyDataSetChanged()
    }
}
