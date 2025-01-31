package com.example.zadaca5

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: Int,
    val title: String,
    val price: Double,
    @SerializedName("description") val description: String,
    @SerializedName("image") val imageUrl: String,
    val isFavorite: Boolean = false
)
