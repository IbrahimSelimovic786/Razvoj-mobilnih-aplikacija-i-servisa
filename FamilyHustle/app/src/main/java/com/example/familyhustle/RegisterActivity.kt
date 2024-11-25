package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        val emailField = findViewById<EditText>(R.id.etRegisterEmail)
        val passwordField = findViewById<EditText>(R.id.etRegisterPassword)
        val registerButton = findViewById<Button>(R.id.btnRegister)

        registerButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
                val savedEmail = sharedPref.getString("email", null)

                if (savedEmail == null) {
                    val editor = sharedPref.edit()
                    editor.putString("email", email)
                    editor.putString("password", password)
                    editor.apply()
                    Log.d("RegisterActivity", "Korisnik uspješno registrovan: $email")
                    Toast.makeText(this, "Registracija uspješna!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Korisnik već postoji", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Unesite email i šifru", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
