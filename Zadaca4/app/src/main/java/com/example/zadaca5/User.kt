package com.example.zadaca5

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users") // Provjeri da li piše "users"
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String
)
