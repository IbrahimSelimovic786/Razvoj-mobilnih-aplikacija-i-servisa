package com.example.zadaca3

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditFragment : Fragment() {

    interface OnNoteSavedListener {
        fun onNoteSaved(note: Note)
        fun onNoteDeleted(note: Note) // Nova metoda za brisanje
    }




    private var note: Note? = null
    private var listener: OnNoteSavedListener? = null

    companion object {
        fun newInstance(note: Note?): EditFragment {
            val fragment = EditFragment()
            fragment.note = note
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNoteSavedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnNoteSavedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit, container, false)

        val titleEditText = view.findViewById<EditText>(R.id.editTitle)
        val textEditText = view.findViewById<EditText>(R.id.editText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        note?.let {
            titleEditText.setText(it.title)
            textEditText.setText(it.text)
        }

        val deleteIcon = view.findViewById<ImageView>(R.id.deleteIcon)

        deleteIcon.setOnClickListener {
            note?.let {
                listener?.onNoteDeleted(it) // Implementirajte novu metodu za brisanje
                parentFragmentManager.popBackStack()
            } ?: Toast.makeText(context, "Nema zabilješke za brisanje", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val text = textEditText.text.toString()

            if (title.isBlank()) {
                Toast.makeText(context, "Zabilješka bez naslova nije sačuvana", Toast.LENGTH_SHORT).show()
            } else {
                val newNote = Note(
                    title = title,
                    text = text,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
                listener?.onNoteSaved(newNote)  // Poziv listenera
                parentFragmentManager.popBackStack()  // Vraćanje na MainFragment
            }
        }
        return view
    }
}
