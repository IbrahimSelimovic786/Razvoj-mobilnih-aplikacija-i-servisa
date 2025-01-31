package com.example.zadaca3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
class MainFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private val notes = mutableListOf<Note>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        noteAdapter = NoteAdapter(notes) { note ->
            // Klik na bilješku otvara EditFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, EditFragment.newInstance(note))
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = noteAdapter

        fun removeNote(note: Note) {
            val position = notes.indexOf(note)
            if (position != -1) {
                notes.removeAt(position)
                noteAdapter.notifyItemRemoved(position)
            }
        }
        // Dugme za dodavanje
        view.findViewById<FloatingActionButton>(R.id.addNoteButton).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, EditFragment.newInstance(null))
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    // Metoda za dodavanje nove bilješke
    fun addNote(note: Note) {
        notes.add(0, note)  // Dodajemo na početak liste
        noteAdapter.notifyItemInserted(0)  // Osvježavamo RecyclerView
    }

    fun removeNote(note: Note) {

        val position = notes.indexOf(note)
        if (position != -1) {
            notes.removeAt(position)
            noteAdapter.notifyItemRemoved(position)
        }
    }

}
