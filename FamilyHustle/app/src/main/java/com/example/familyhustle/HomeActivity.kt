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

        findViewById<android.widget.ImageView>(R.id.ic_calendar).setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            intent.putExtra("selectedHouse", selectedHouse)
            startActivity(intent)
        }

        rvWeekHistory.layoutManager = LinearLayoutManager(this)
        weekAdapter = WeekAdapter(weekList) { week ->
            val weekIndex = weekList.indexOf(week) + 1
            showTasksForSelectedHouseAndWeek(selectedHouse, weekIndex)
        }
        rvWeekHistory.adapter = weekAdapter

        rvTasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(taskList)
        rvTasks.adapter = taskAdapter

        loadHouses()
        loadWeekHistory()

        houseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedHouse = houseSpinner.selectedItem.toString()
                clearTasks() // Očisti taskove dok se ne klikne na sedmicu
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

        taskList.clear()
        taskMap.clear()

        allTasks.values.forEach { value ->
            val taskData = value.toString().split("|")
            if (taskData.size >= 4) {
                val title = taskData[0]
                val points = taskData[1]
                val deadline = taskData[2]
                val houseName = taskData[3]

                if (houseName != selectedHouse) return@forEach

                val week = calculateWeek(deadline)
                Log.d("HomeActivity", "Task '$title' za kuću '$houseName' pada u sedmicu $week.")

                if (week != -1) {
                    taskMap.getOrPut(houseName) { mutableMapOf() }
                        .getOrPut(week) { mutableListOf() }
                        .add(Task(title, points, deadline))
                }
            }
        }
    }

    private fun calculateWeek(dateString: String): Int {
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.minimalDaysInFirstWeek = 4 // ISO-8601 standard

        return try {
            val date = formatter.parse(dateString)
            calendar.time = date

            // Dohvatamo početak i kraj sedmice
            val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)

            // Vraćamo ispravan broj sedmice
            weekOfMonth
        } catch (e: Exception) {
            Log.e("HomeActivity", "Greška prilikom parsiranja datuma: $dateString", e)
            -1
        }
    }


    private fun showTasksForSelectedHouseAndWeek(house: String?, week: Int) {
        if (house == null) {
            taskList.clear()
            taskAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Odaberite kuću", Toast.LENGTH_SHORT).show()
            return
        }

        val tasksForWeek = taskMap[house]?.get(week) ?: mutableListOf()
        Log.d("HomeActivity", "Prikaz zadataka za kuću '$house' i sedmicu $week: ${tasksForWeek.size} zadataka.")

        taskList.clear()
        taskList.addAll(tasksForWeek)
        taskAdapter.notifyDataSetChanged()
    }

    private fun clearTasks() {
        taskList.clear()
        taskAdapter.notifyDataSetChanged()
    }
}
