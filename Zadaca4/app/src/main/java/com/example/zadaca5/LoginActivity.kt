package com.example.zadaca5

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.zadaca5.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicijalizacija baze
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app-database"
        ).fallbackToDestructiveMigration()  // Briše bazu ako postoji konflikt
            .build()

        // Dodavanje testnog korisnika prilikom pokretanja aplikacije
        lifecycleScope.launch(Dispatchers.IO) {
            val existingUser = db.userDao().getUser("testuser", "password123")
            if (existingUser == null) {
                db.userDao().insertUser(User(username = "testuser", password = "password123"))
            }
        }

        // Login dugme
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            lifecycleScope.launch(Dispatchers.IO) {
                val user = db.userDao().getUser(username, password)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
