package com.example.familyhustle

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.familyhustle.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicijalizacija SharedPreferences
        sharedPreferences = getSharedPreferences("FamilyHustlePrefs", MODE_PRIVATE)

        // Provjera da li je korisnik označio "Remember Me"
        val isRemembered = sharedPreferences.getBoolean("rememberMe", false)
        if (isRemembered) {
            val email = sharedPreferences.getString("email", null)
            val password = sharedPreferences.getString("password", null)
            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                autoLogin(email, password)
            }
        }

        // Klik na dugme "Continue"
        binding.btnContinue.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val rememberMe = binding.cbRememberMe.isChecked

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Spremi korisničke podatke ako je "Remember Me" označen
                            if (rememberMe) {
                                saveCredentials(email, password)
                            } else {
                                clearCredentials()
                            }
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Login failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim() // Preuzimanje emaila iz polja za unos
            if (email.isNotEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Reset password email sent to $email", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "Failed to send reset email: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Automatska prijava
    private fun autoLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Auto login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // Spremanje korisničkih podataka
    private fun saveCredentials(email: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("rememberMe", true)
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
    }

    // Brisanje korisničkih podataka
    private fun clearCredentials() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
