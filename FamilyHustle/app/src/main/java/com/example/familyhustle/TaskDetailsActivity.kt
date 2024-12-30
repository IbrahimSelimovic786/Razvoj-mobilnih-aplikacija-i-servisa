package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TaskDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        val taskTitle = intent.getStringExtra("taskTitle")
        val taskDescription = intent.getStringExtra("taskDescription")

        findViewById<TextView>(R.id.tvTaskTitle).text = taskTitle
        findViewById<TextView>(R.id.tvTaskDescription).text = taskDescription

        findViewById<Button>(R.id.btnTakeBeforePhoto).setOnClickListener {
            Toast.makeText(this, "Take Before Photo", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnTakeAfterPhoto).setOnClickListener {
            Toast.makeText(this, "Take After Photo", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnMarkAsComplete).setOnClickListener {
            val intent = Intent()
            intent.putExtra("taskTitle", taskTitle)
            intent.putExtra("taskCompleted", true)  // Dodajemo informaciju o statusu zadatka

            // Spremi status zadatka u SharedPreferences
            val sharedPref = getSharedPreferences("TasksPref", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(taskTitle, true) // Spremamo da je zadatak obavljen
            editor.apply()

            setResult(RESULT_OK, intent)
            finish()
        }

        findViewById<Button>(R.id.btnMarkAsComplete).setOnClickListener {
            val intent = Intent()

            // Spremi status zadatka u SharedPreferences
            val sharedPref = getSharedPreferences("TasksPref", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(taskTitle, true) // Sprema status kao obavljen
            editor.apply()

            // Proslijedi informaciju o zadatku
            intent.putExtra("taskTitle", taskTitle)
            intent.putExtra("taskCompleted", true)
            setResult(RESULT_OK, intent)
            finish()
        }

    }
}
