package com.example.familyhustle.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class House(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val Housename: String
)