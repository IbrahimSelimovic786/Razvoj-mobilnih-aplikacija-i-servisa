package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var houseSpinner: Spinner
    private lateinit var etTaskName: EditText
    private lateinit var etTaskPoints: EditText
    private lateinit var etTaskDate: EditText
    private var selectedHouse: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        houseSpinner = findViewById(R.id.spinnerHouse)
        etTaskName = findViewById(R.id.etTaskName)
        etTaskPoints = findViewById(R.id.etTaskPoints)
        etTaskDate = findViewById(R.id.etTaskDate)

        val btnSaveTask = findViewById<Button>(R.id.btnCreateTask)
        val btnCreateHouse = findViewById<Button>(R.id.btnCreateHouse)

        loadHouses()

        houseSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedHouse = houseSpinner.selectedItem.toString()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        btnSaveTask.setOnClickListener {
            saveTask()
        }

        btnCreateHouse.setOnClickListener {
            val intent = Intent(this, CreateHouseActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadHouses()
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
            Toast.makeText(this, "Nema dostupnih kuća. Kreirajte kuću.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveTask() {
        val taskName = etTaskName.text.toString().trim()
        val taskPoints = etTaskPoints.text.toString().trim()
        val taskDate = etTaskDate.text.toString().trim()

        if (taskName.isEmpty() || taskPoints.isEmpty() || taskDate.isEmpty() || selectedHouse == null) {
            Toast.makeText(this, "Unesite sve podatke i odaberite kuću.", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("TasksPref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val taskWeek = try {
            val deadlineDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(taskDate)
            val calendar = Calendar.getInstance()
            calendar.time = deadlineDate
            calendar.get(Calendar.WEEK_OF_MONTH)
        } catch (e: Exception) {
            Toast.makeText(this, "Pogrešan format datuma. Koristite DD-MM-YYYY.", Toast.LENGTH_SHORT).show()
            return
        }

        val taskKey = "$selectedHouse|$taskName|$taskPoints|$taskDate|$taskWeek"
        editor.putString(taskKey, "$taskName|$taskPoints|$taskDate|$selectedHouse|$taskWeek")
        editor.apply()

        Toast.makeText(this, "Task '$taskName' uspješno kreiran za kuću '$selectedHouse'.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
