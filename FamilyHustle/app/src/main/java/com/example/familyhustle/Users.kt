package com.example.familyhustle

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class UserListActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.users)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        val listView = findViewById<ListView>(R.id.lvUsers)
        val users = getAllUsers()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, users)
        listView.adapter = adapter
    }

    private fun getAllUsers(): List<String> {
        val allEntries = sharedPreferences.all
        val userList = mutableListOf<String>()

        for ((key, value) in allEntries) {
            userList.add("Email: $key\nPassword: $value")
        }

        return userList
    }
}
