package com.example.familyhustle

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.familyhustle.databinding.ActivityTaskBinding
import com.google.firebase.database.FirebaseDatabase

class TaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBinding
    private val database = FirebaseDatabase.getInstance().reference
    private val houses = mutableListOf<String>() // Lista kuća

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadHouses()

        binding.btnSaveTask.setOnClickListener {
            val houseName = binding.spinnerHouse.selectedItem.toString()
            val taskName = binding.etTaskName.text.toString()
            val dueDate = binding.etDueDate.text.toString()
            val taskDescription = binding.etTaskDescription.text.toString()

            if (taskName.isNotEmpty() && dueDate.isNotEmpty() && taskDescription.isNotEmpty()) {
                saveTaskToFirebase(taskName, dueDate, taskDescription, houseName)
            } else {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadHouses() {
        val houseRef = database.child("houses")
        houseRef.get().addOnSuccessListener { snapshot ->
            houses.clear()
            for (houseSnapshot in snapshot.children) {
                val houseName = houseSnapshot.child("name").getValue(String::class.java)
                if (houseName != null) {
                    houses.add(houseName)
                }
            }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, houses)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerHouse.adapter = adapter
        }.addOnFailureListener { error ->
            Toast.makeText(this, "Failed to load houses: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTaskToFirebase(name: String, dueDate: String, description: String, houseName: String){
        val pointsInput = binding.etTaskPoints.text.toString()
        val points = pointsInput.toInt()
        val taskRef = database.child("tasks").push()
        val task = TaskData(
            id = taskRef.key ?: "",
            name = name,
            description = description,
            dueDate = dueDate,
            houseName = houseName,
            weekNumber = getWeekNumber(dueDate),
            points = points // Dodaj bodove
        )

        taskRef.setValue(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error saving task: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getWeekNumber(date: String): Int {
        return try {
            val day = date.split("-")[2].toInt()
            when (day) {
                in 1..7 -> 1
                in 8..14 -> 2
                in 15..21 -> 3
                else -> 4
            }
        } catch (e: Exception) {
            -1 // Vraća -1 ako je datum neispravan
        }
    }
}
