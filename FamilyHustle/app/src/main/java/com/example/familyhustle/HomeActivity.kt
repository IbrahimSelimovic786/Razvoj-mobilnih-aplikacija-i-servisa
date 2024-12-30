package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private val taskMap = mutableMapOf<String, MutableMap<Int, MutableList<Task>>>()
    private lateinit var houseSpinner: Spinner
    private lateinit var rvWeekHistory: RecyclerView
    private lateinit var rvTasks: RecyclerView
    private lateinit var weekAdapter: WeekAdapter
    private lateinit var taskAdapter: TaskAdapter
    private val weekList = mutableListOf<Week>()
    private val taskList = mutableListOf<Task>()
    private var selectedHouse: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        houseSpinner = findViewById(R.id.spinnerHouses)
        rvWeekHistory = findViewById(R.id.rvWeekHistory)
        rvTasks = findViewById(R.id.rvTasks)

        // Postavljanje listenera za Calendar ikonicu
        findViewById<android.widget.ImageView>(R.id.ic_calendar).setOnClickListener {
            if (selectedHouse != null) {
                val intent = Intent(this, CreateTaskActivity::class.java)
                intent.putExtra("selectedHouse", selectedHouse)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Prvo odaberite kuću!", Toast.LENGTH_SHORT).show()
            }
        }

        // Postavljanje RecyclerView-a za sedmice
        rvWeekHistory.layoutManager = LinearLayoutManager(this)
        weekAdapter = WeekAdapter(weekList) { week ->
            val weekIndex = weekList.indexOf(week) + 1
            showTasksForSelectedHouseAndWeek(selectedHouse, weekIndex)
        }
        rvWeekHistory.adapter = weekAdapter

        // Postavljanje RecyclerView-a za zadatke
        rvTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(taskList) { task ->
            val intent = Intent(this, TaskDetailsActivity::class.java)
            intent.putExtra("taskTitle", task.title)
            intent.putExtra("taskDescription", task.description)
            startActivityForResult(intent, 1) // Pokreće aktivnost za rezultat
        }
        rvTasks.adapter = taskAdapter

        loadHouses()
        loadWeekHistory()

        // Listener za spinner kuća
        houseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedHouse = houseSpinner.selectedItem.toString()
                loadTasks()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        loadHouses()
        loadTasks()
    }

    private fun loadHouses() {
        val sharedPref = getSharedPreferences("HousesPref", MODE_PRIVATE)
        val houses = sharedPref.all.keys.toList()
        if (houses.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, houses)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            houseSpinner.adapter = adapter
            selectedHouse = houses.firstOrNull()
        } else {
            selectedHouse = null
            houseSpinner.adapter = null
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == 1) { // 1 je requestCode za task detail activity
            val taskTitle = data?.getStringExtra("taskTitle")
            val taskCompleted = data?.getBooleanExtra("taskCompleted", false) ?: false

            // Ažuriraj zadatak u listi
            taskList.find { it.title == taskTitle }?.let {
                it.isCompleted = taskCompleted
                taskAdapter.notifyDataSetChanged() // Obavještava adapter da je lista promijenjena
            }
        }
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
        if (selectedHouse == null) return

        val sharedPref = getSharedPreferences("TasksPref", MODE_PRIVATE)
        val allTasks = sharedPref.all

        taskMap.clear()

        allTasks.values.forEach { value ->
            val taskData = value.toString().split("|")
            if (taskData.size >= 5) {
                val title = taskData[0]
                val points = taskData[1]
                val deadline = taskData[2]
                val houseName = taskData[3]
                val week = taskData[4].toIntOrNull() ?: return@forEach

                if (houseName == selectedHouse) {
                    taskMap.getOrPut(houseName) { mutableMapOf() }
                        .getOrPut(week) { mutableListOf() }
                        .add(Task(title, points, deadline, isCompleted = sharedPref.getBoolean(title, false))) // Provjeravamo SharedPreferences
                }
            }
        }
    }


    private fun showTasksForSelectedHouseAndWeek(house: String?, week: Int) {
        if (house == null) {
            taskList.clear()
            taskAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Odaberite kuću", Toast.LENGTH_SHORT).show()
            return
        }

        val tasksForSelectedWeek = taskMap[house]?.get(week) ?: emptyList()
        taskList.clear()
        taskList.addAll(tasksForSelectedWeek)
        taskAdapter.notifyDataSetChanged()
    }
}
