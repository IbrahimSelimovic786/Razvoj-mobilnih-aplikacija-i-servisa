package com.example.zadaca5

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zadaca5.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.room.Room

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: ProductAdapter
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app-database"
        ).fallbackToDestructiveMigration()
            .build()

        apiService = Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/") // API za testiranje
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        adapter = ProductAdapter { product ->
            lifecycleScope.launch(Dispatchers.IO) {
                db.productDao().updateFavorite(product.id, !product.isFavorite)
                loadProductsFromDB()
            }
        }


        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Poziva se funkcija za učitavanje velikog broja proizvoda
        loadProductsFromApi()
    }

    private fun loadProductsFromApi() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val products = apiService.getProducts()
                db.productDao().insertProducts(products)  // Ubacivanje više proizvoda odjednom
                loadProductsFromDB()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error loading from API", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadProductsFromDB() {
        lifecycleScope.launch(Dispatchers.IO) {
            val products = db.productDao().getAllProducts()
            withContext(Dispatchers.Main) {
                if (products.isNotEmpty()) {
                    adapter.submitList(products)
                } else {
                    Toast.makeText(this@MainActivity, "No products found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
