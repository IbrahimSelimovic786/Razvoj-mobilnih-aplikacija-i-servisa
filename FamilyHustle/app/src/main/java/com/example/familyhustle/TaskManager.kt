package com.example.familyhustle

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object TaskManager {
    private val database: FirebaseDatabase = Firebase.database
    const val TASKS_PATH = "tasks"

    fun addTask(task: TaskData) {
        val taskId = database.getReference(TASKS_PATH).push().key
        taskId?.let {
            val newTask = task.copy(id = it) // Dodaj ID u zadatak
            database.getReference("$TASKS_PATH/$it").setValue(newTask)
        }
    }

    fun getTasks(callback: (List<TaskData>) -> Unit) {
        val taskList = mutableListOf<TaskData>()
        database.getReference(TASKS_PATH).get()
            .addOnSuccessListener { snapshot ->
                snapshot.children.forEach { data ->
                    val task = data.getValue(TaskData::class.java)
                    task?.let { taskList.add(it) }
                }
                callback(taskList)
            }
            .addOnFailureListener { error ->
                callback(emptyList()) // Vraća praznu listu u slučaju greške
            }
    }
}
