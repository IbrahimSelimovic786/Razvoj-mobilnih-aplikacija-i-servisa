package com.example.zadaca3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
class MainActivity : AppCompatActivity(), EditFragment.OnNoteSavedListener {

    private lateinit var mainFragment: MainFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        if (savedInstanceState == null) {
            mainFragment = MainFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mainFragment)
                .commit()
        }
        val infoIcon = findViewById<ImageView>(R.id.infoIcon)
        infoIcon.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AboutFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onNoteDeleted(note: Note) {
        mainFragment.removeNote(note) // Pretpostavljamo da `MainFragment` ima metodu za uklanjanje bilješke
    }

    override fun onNoteSaved(note: Note) {
        // Prosleđujemo novu bilješku MainFragment-u
        mainFragment.addNote(note)
    }
}
