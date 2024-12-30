package com.example.familyhustle

data class Task(val title: String,
                val points: String,
                val description: String,
                var isCompleted: Boolean = false)
