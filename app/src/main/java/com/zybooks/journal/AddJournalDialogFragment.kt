package com.zybooks.journal

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random


class AddJournalDialogFragment: DialogFragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationImageView: ImageView
    private lateinit var locationTextView: TextView
    private lateinit var createButton: Button
    private lateinit var cancelButton: Button

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000 // Update interval in milliseconds (e.g., every 10 seconds)
        fastestInterval = 5000 // Fastest update interval in milliseconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d("LocationCode", "onRequestPermissionsResult")
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LocationCode", "permission granted")

                // Disable buttons
                createButton.isEnabled = false
                cancelButton.isEnabled = false
                // Location permission granted, start location updates
                requestContinuousLocationUpdates()
            } else {
                Log.d("LocationCode", "permission not granted")
                // Location permission denied
                Toast.makeText(
                    requireContext(),
                    "Allow location permission in settings to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
                locationTextView.setText(R.string.allow_location_permissions);
            }
        }
    }

//    private fun getCityName(latitude: Double, longitude: Double): String {
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
//        val cityName = addresses!![0].getAddressLine(0)
//        val stateName = addresses[0].getAddressLine(1)
//        val countryName = addresses[0].getAddressLine(2)
//        Log.d("City", cityName)
//        Log.d("State", stateName)
//        Log.d("Country", countryName)
//
//        return cityName
//    }

    private fun requestContinuousLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationTextView.setText(R.string.finding_location)
            val locationCallback = object : LocationCallback() {
                @SuppressLint("StringFormatInvalid")
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    Log.d("LocationCode: onLocationResult", locationResult.locations.toString())
                    for (location in locationResult.locations) {
                        val latitude = location.latitude.toString().substring(0, 6)
                        val longitude = location.longitude.toString().substring(0, 6)
                        // Log.d("City, State", getCityName(latitude.toDouble(), longitude.toDouble()))
                        if(isAdded) {
                            val locationText =
                                getString(R.string.location_info, latitude, longitude)
                            locationTextView.text = locationText

                            createButton.isEnabled = true
                            cancelButton.isEnabled = true

                            locationImageView.setOnClickListener(null)
                        }
                        return // Stop processing after handling the first location
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } else {
            locationTextView.setText(R.string.allow_location_permissions);
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // find the dialog and inflate the DialogFragment layout
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_new_journal, null)

            val scrollView = ScrollView(requireContext())
            scrollView.addView(view)

            val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
            val currentDate = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date())

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            locationImageView = view.findViewById(R.id.locationImage)
            locationTextView = view.findViewById(R.id.locationText)

            locationImageView.setOnClickListener {
                Log.d("LocationCode", "Image onClick function")
                requestContinuousLocationUpdates()
            }

            dateTextView.text = currentDate

            createButton = view.findViewById<Button>(R.id.btnCreate)
            cancelButton = view.findViewById<Button>(R.id.btnCancel)

            // create a new journal object when user clicks on Create button of dialog
            createButton.setOnClickListener {
                val editJournalText = view.findViewById<EditText>(R.id.editTextJournal)

                val journalText = editJournalText.text.toString().trim()
                val journalLocation = locationTextView.text.toString().trim()

                if (journalText.isNotEmpty()) {
                    val newJournal = Journal(Random().nextInt().toString(), journalText, Date(), journalLocation)
                    (activity as? MainActivity)?.onAddNewJournal(newJournal)
                }
            }

            builder.setView(scrollView)
            return builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }
}