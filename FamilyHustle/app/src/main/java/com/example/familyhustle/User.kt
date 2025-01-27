package com.example.familyhustle.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val email: String,
    val username: String,
    val password: String,
    val role: String
)
