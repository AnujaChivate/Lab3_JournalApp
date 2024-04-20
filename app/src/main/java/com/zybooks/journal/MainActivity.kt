package com.zybooks.journal

import JournalViewModel
import android.content.Intent
import android.content.res.Configuration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    // floating action button to add a new Journal
    private lateinit var floatingActionBtn: FloatingActionButton

    // Layout to hold list of journals
    private lateinit var journalEntriesLayout: LinearLayout

    // Layout for no journals (empty state)
    private lateinit var emptyJournalLayout: ConstraintLayout
    private lateinit var logoImageView: ImageView
    private lateinit var emptyJournalHeaderTextView: TextView
    private lateinit var emptyJournalSubHeader1TextView: TextView
    private lateinit var emptyJournalSubHeader2TextView: TextView

    // dialog fragment to create new journal entry
    private lateinit var dialog: AddJournalDialogFragment

    // list to store all journals
    private var viewModel = JournalViewModel(CopyOnWriteArrayList())
    private val addedJournalIds = mutableListOf<Int>()

    // sound pool
    private lateinit var soundEffects: SoundEffects

    private lateinit var meditateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the fragment was previously retained
        dialog = lastCustomNonConfigurationInstance as? AddJournalDialogFragment
            ?: AddJournalDialogFragment()

        // floating button to add new journal
        floatingActionBtn = findViewById(R.id.addJournal)

        // layout to hold all journals in the list
        journalEntriesLayout = findViewById(R.id.journalList)

        // layout to show when no journals are created
        emptyJournalLayout = findViewById(R.id.emptyJournalLayout)

        // empty layout views
        logoImageView = findViewById(R.id.logoImageView)
        emptyJournalHeaderTextView = findViewById(R.id.emptyJournalHeader)
        emptyJournalSubHeader1TextView = findViewById(R.id.emptyJournalSubHeader1)
        emptyJournalSubHeader2TextView = findViewById(R.id.emptyJournalSubHeader2)

        soundEffects = SoundEffects.getInstance(applicationContext)
        meditateButton = findViewById(R.id.meditateButton)

        meditateButton.setOnClickListener {
            startActivity(Intent(this, Meditation::class.java))
        }

        // Always make sure the media player is not running meditation music
        // on main activity
        val workRequest = OneTimeWorkRequestBuilder<MediaPlayerWorker>()
            .setInputData(Data.Builder().putBoolean("isPlaying", false).build())
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)

        viewModel = ViewModelProvider(this).get(JournalViewModel::class.java)

        // if activity is recreated because of screen orientation change, restore saved list of journal items if any
        viewModel.journalList.addAll(savedInstanceState?.getParcelableArrayList("journalList") ?: emptyList())

        // Add each journal entry to the ViewModel's list
        if (viewModel.journalList.isNotEmpty()) {
            for (journalItem in viewModel.journalList) {
                onAddNewJournal(journalItem)
            }
        } else {
            // set visibility of empty view state when main activity is created
            emptyJournalLayout.visibility = View.VISIBLE
            logoImageView.visibility = View.VISIBLE
            emptyJournalHeaderTextView.visibility = View.VISIBLE
            emptyJournalSubHeader1TextView.visibility = View.VISIBLE
            emptyJournalSubHeader2TextView.visibility = View.VISIBLE
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Reinitialize views after orientation change
        emptyJournalLayout = findViewById(R.id.emptyJournalLayout)
        logoImageView = findViewById(R.id.logoImageView)
        emptyJournalHeaderTextView = findViewById(R.id.emptyJournalHeader)
        emptyJournalSubHeader1TextView = findViewById(R.id.emptyJournalSubHeader1)
        emptyJournalSubHeader2TextView = findViewById(R.id.emptyJournalSubHeader2)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the list of journal items
        outState.putParcelableArrayList("journalList", ArrayList(viewModel.journalList))
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
                // code to delete the Journal based on Id
                val journalId = item.intent?.getIntExtra("journalId", -1) ?: -1
                if (journalId != -1) {
                    viewModel.journalList.removeAll { it.id.toInt() == journalId }
                    refreshJournalList()
                }
                soundEffects.playJournalDeleted()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onDestroy() {
        // Dismiss the fragment and remove it from the FragmentManager
        if (isFinishing) {
            dialog.dismiss()
        }
        super.onDestroy()
        soundEffects.release()
    }

    override fun onRetainCustomNonConfigurationInstance(): Any {
        // Retain the fragment instance during configuration changes
        return dialog
    }

    fun onOpenNewJournalDialog(view: View) {
        // open new journal dialog fragment
        dialog.show(supportFragmentManager, "addJournalDialog")
    }

    fun onCloseNewJournalDialog(view: View) {
        // close journal dialog
        dialog.dismiss()
    }

    fun onAddNewJournal(newJournal: Journal) {
        viewModel.journalList.add(newJournal)
        // hide the empty state layout
        emptyJournalLayout.visibility = View.GONE
        // append created journal and refresh the view
        refreshJournalList()
        dialog.dismiss()
        soundEffects.playJournalCreated()
    }

    fun showContextMenu(view: View) {
        openContextMenu(view)
    }

    private fun refreshJournalList() {
        // Clear the existing journal entries
        journalEntriesLayout.removeAllViews()

        // Reset the list of added journal IDs
        addedJournalIds.clear()

        // Add each journal entry to the LinearLayout
        for (journalItem in viewModel.journalList.asReversed()) {
            if (!addedJournalIds.contains(journalItem.id.toInt())) {
                val journalItemView = layoutInflater.inflate(R.layout.journal_item, null)

                journalItemView.id = journalItem.id.toInt()

                val journalTextView = journalItemView.findViewById<TextView>(R.id.textViewJournalText)
                journalTextView.text = journalItem.text

                val journalTextViewLocation = journalItemView.findViewById<TextView>(R.id.textViewJournalLocation)
                journalTextViewLocation.text = journalItem.location

                val journalDateView = journalItemView.findViewById<TextView>(R.id.textViewJournalDate)
                journalDateView.text =
                    SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(journalItem.dateTime)

                registerForContextMenu(journalItemView)
                journalEntriesLayout.addView(journalItemView)

                // Add the ID of the added journal to the list
                addedJournalIds.add(journalItem.id.toInt())
            }
        }

        // If no journals available, show empty state layout
        if (viewModel.journalList.isEmpty()) {
            emptyJournalLayout.visibility = View.VISIBLE
            logoImageView.visibility = View.VISIBLE
            emptyJournalHeaderTextView.visibility = View.VISIBLE
            emptyJournalSubHeader1TextView.visibility = View.VISIBLE
            emptyJournalSubHeader2TextView.visibility = View.VISIBLE
        }
    }
}