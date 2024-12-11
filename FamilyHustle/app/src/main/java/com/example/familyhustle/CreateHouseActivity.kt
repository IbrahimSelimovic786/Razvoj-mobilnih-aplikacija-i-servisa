package com.example.familyhustle

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CreateHouseActivity : AppCompatActivity() {

    private val memberList = mutableListOf<Member>() // Lista članova kuće
    private lateinit var memberAdapter: MemberAdapter

    override fun onBackPressed() {
        val sharedPref = getSharedPreferences("HousesPref", MODE_PRIVATE)
        val allHouses = sharedPref.all.keys.toList()

        val resultIntent = Intent()
        resultIntent.putStringArrayListExtra("houses", ArrayList(allHouses))
        setResult(RESULT_OK, resultIntent)

        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_house_activity)

        val etHouseName = findViewById<EditText>(R.id.etHouseName)
        val etMemberEmail = findViewById<EditText>(R.id.etMemberEmail)
        val roleSpinner = findViewById<Spinner>(R.id.roleSpinner)
        val rvMembers = findViewById<RecyclerView>(R.id.rvMembers)
        val btnAddMember = findViewById<Button>(R.id.btnAddMember)
        val btnSaveHouse = findViewById<Button>(R.id.btnSaveHouse)

        // Postavljanje opcija za spinner (uloge)
        val roles = listOf("Roditelj", "Ukućan")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = roleAdapter

        // Postavljanje RecyclerView-a za članove
        rvMembers.layoutManager = LinearLayoutManager(this)
        memberAdapter = MemberAdapter(memberList)
        rvMembers.adapter = memberAdapter

        // Dodavanje člana
        btnAddMember.setOnClickListener {
            val email = etMemberEmail.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (email.isNotEmpty()) {
                memberList.add(Member(email, role))
                memberAdapter.notifyDataSetChanged()
                etMemberEmail.text.clear()
                Toast.makeText(this, "Član dodan: $email ($role)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Unesite email člana", Toast.LENGTH_SHORT).show()
            }
        }

        // Spremanje kuće
        btnSaveHouse.setOnClickListener {
            val houseName = etHouseName.text.toString().trim()

            if (houseName.isNotEmpty() && memberList.isNotEmpty()) {
                val sharedPref = getSharedPreferences("HousesPref", MODE_PRIVATE)
                val editor = sharedPref.edit()
                val membersData = memberList.joinToString(",") { "${it.email}:${it.role}" }
                editor.putString(houseName, membersData)
                editor.apply()

                Toast.makeText(this, "Kuća '$houseName' uspješno dodana!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Unesite naziv kuće i dodajte članove", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
