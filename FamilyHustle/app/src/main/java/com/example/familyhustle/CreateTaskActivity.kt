package com.example.familyhustle

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        val etTaskName = findViewById<EditText>(R.id.etTaskName)
        val etTaskDate = findViewById<EditText>(R.id.etTaskDate)
        val etTaskPoints = findViewById<EditText>(R.id.etTaskPoints)
        val etTaskDescription = findViewById<EditText>(R.id.etTaskDescription)
        val btnCreateTask = findViewById<Button>(R.id.btnCreateTask)

        // Date Picker za unos datuma
        etTaskDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$dayOfMonth-${month + 1}-$year"
                    etTaskDate.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Kreiranje zadatka
        btnCreateTask.setOnClickListener {
            val taskName = etTaskName.text.toString().trim()
            val taskDate = etTaskDate.text.toString().trim()
            val taskPoints = etTaskPoints.text.toString().trim()
            val taskDescription = etTaskDescription.text.toString().trim()

            // Provera da li su polja popunjena
            if (taskName.isEmpty() || taskDate.isEmpty() || taskPoints.isEmpty() || taskDescription.isEmpty()) {
                Toast.makeText(this, "Popunite sva polja!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Provera datuma
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val deadlineDate = try {
                dateFormat.parse(taskDate)
            } catch (e: Exception) {
                Toast.makeText(this, "Neispravan format datuma!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (deadlineDate == null) {
                Toast.makeText(this, "Neispravan datum!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Spremanje u SharedPreferences
            try {
                val sharedPref: SharedPreferences = getSharedPreferences("TasksPref", MODE_PRIVATE)
                val editor = sharedPref.edit()

                // Kreiranje jedinstvenog ključa za svaki zadatak
                val taskKey = "task_${System.currentTimeMillis()}"
                val taskData = "$taskName|$taskPoints|$taskDate|$taskDescription"
                editor.putString(taskKey, taskData)
                editor.apply()

                Toast.makeText(this, "Zadatak uspešno kreiran!", Toast.LENGTH_SHORT).show()
                finish() // Vraćanje na prethodnu aktivnost
            } catch (e: Exception) {
                Toast.makeText(this, "Došlo je do greške pri kreiranju zadatka.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
