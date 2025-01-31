package com.example.zadaca5

import retrofit2.http.GET

interface ApiService {
    @GET("products") // Provjeri rutu API-ja (koristi pravi endpoint)
    suspend fun getProducts(): List<Product>
}
