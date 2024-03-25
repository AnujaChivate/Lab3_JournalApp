package com.zybooks.journal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var floatingActionBtn: FloatingActionButton

    private lateinit var journalEntriesLayout: LinearLayout

    private lateinit var emptyJournalLayout: ConstraintLayout
    private lateinit var logoImageView: ImageView
    private lateinit var emptyJournalHeaderTextView: TextView
    private lateinit var emptyJournalSubHeader1TextView: TextView
    private lateinit var emptyJournalSubHeader2TextView: TextView

    private val dialog: AddJournalDialogFragment = AddJournalDialogFragment()

    private val journalList = mutableListOf<Journal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        floatingActionBtn = findViewById(R.id.addJournal)

        journalEntriesLayout = findViewById(R.id.journalList)

        emptyJournalLayout = findViewById(R.id.emptyJournalLayout)
        logoImageView = findViewById(R.id.logoImageView)
        emptyJournalHeaderTextView = findViewById(R.id.emptyJournalHeader)
        emptyJournalSubHeader1TextView = findViewById(R.id.emptyJournalSubHeader1)
        emptyJournalSubHeader2TextView = findViewById(R.id.emptyJournalSubHeader2)

        emptyJournalLayout.visibility = View.VISIBLE
        logoImageView.visibility = View.VISIBLE
        emptyJournalHeaderTextView.visibility = View.VISIBLE
        emptyJournalSubHeader1TextView.visibility = View.VISIBLE
        emptyJournalSubHeader2TextView.visibility = View.VISIBLE
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v != null) {
            menuInflater.inflate(R.menu.context_menu, menu)
            // Set the journalId as the itemId of the context menu item
            val journalId = v.id
            menu?.findItem(R.id.deleteJournal)?.intent = Intent().apply {
                putExtra("journalId", journalId)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.deleteJournal -> {
                val journalId = item.intent?.getIntExtra("journalId", -1) ?: -1
                if (journalId != -1) {
                    val iterator = journalList.iterator()
                    while (iterator.hasNext()) {
                        val journal = iterator.next()
                        if (journal.id == journalId.toString()) {
                            iterator.remove()
                            break
                        }
                    }
                    refreshJournalList()
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    fun onOpenNewJournalDialog(view: View) {
        dialog.show(supportFragmentManager, "addJournalDialog")
    }

    fun onCloseNewJournalDialog(view: View) {
        dialog.dismiss()
    }

    fun onAddNewJournal(view: View, newJournal: Journal) {
        journalList.add(newJournal)

        emptyJournalLayout.visibility = View.GONE

        refreshJournalList()

        onCloseNewJournalDialog(view)
    }

    fun showContextMenu(view: View) {
        openContextMenu(view)
    }

    private fun refreshJournalList() {
        // Clear the existing journal entries
        journalEntriesLayout.removeAllViews()

        // Add each journal entry to the LinearLayout
        for (journalItem in journalList) {
            val journalItemView = layoutInflater.inflate(R.layout.journal_item, null)

            journalItemView.id = journalItem.id.toInt()

            val journalTextView = journalItemView.findViewById<TextView>(R.id.textViewJournalText)
            journalTextView.text = journalItem.text

            val journalDateView = journalItemView.findViewById<TextView>(R.id.textViewJournalDate)
            journalDateView.text = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(journalItem.dateTime)

            registerForContextMenu(journalItemView)
            journalEntriesLayout.addView(journalItemView)
        }

        if (journalList.size == 0) {
            emptyJournalLayout.visibility = View.VISIBLE
            logoImageView.visibility = View.VISIBLE
            emptyJournalHeaderTextView.visibility = View.VISIBLE
            emptyJournalSubHeader1TextView.visibility = View.VISIBLE
            emptyJournalSubHeader2TextView.visibility = View.VISIBLE
        }
    }
}