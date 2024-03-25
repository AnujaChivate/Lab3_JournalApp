package com.zybooks.journal

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.UUID

class AddJournalDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_new_journal, null)

            val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
            val currentDate = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date())

            dateTextView.text = currentDate

            val createButton = view.findViewById<Button>(R.id.btnCreate)
            createButton.setOnClickListener {
                val editJournalText = view.findViewById<EditText>(R.id.editTextJournal)

                val journalText = editJournalText.text.toString().trim()

                if (journalText.isNotEmpty()) {
                    val newJournal = Journal(Random().nextInt().toString(), journalText, Date())
                    (activity as? MainActivity)?.onAddNewJournal(view, newJournal)
                }
            }

            builder.setView(view)
            return builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}