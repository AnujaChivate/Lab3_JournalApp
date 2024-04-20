package com.zybooks.journal

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class Meditation : AppCompatActivity() {

    private var timer: CountDownTimer? = null

    private lateinit var meditationImages: ImageView
    private lateinit var meditationAnimation: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meditation)

        meditationImages = findViewById(R.id.meditationAnimation)
        meditationImages.setBackgroundResource(R.drawable.breathing)
        meditationAnimation = meditationImages.background as AnimationDrawable

        val meditationDuration = 5 * 60 * 1000L

        // Get references to UI elements
        val timerText = findViewById<TextView>(R.id.meditationTimer)
        val stopButton = findViewById<Button>(R.id.stopMeditation)

        timer = object : CountDownTimer(meditationDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)
                timerText.text = formattedTime
            }

            override fun onFinish() {
                stopMediaPlayer()
                finish()
            }
        }

        (timer as CountDownTimer).start()
        meditationAnimation.start()

        stopButton.setOnClickListener {
            timer?.cancel()
            stopMediaPlayer()
            finish()
        }

        val workRequest = OneTimeWorkRequestBuilder<MediaPlayerWorker>()
            .setInputData(Data.Builder().putBoolean("isPlaying", true).build())
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun sendRequestForMediaPlayer() {
        val workRequest = OneTimeWorkRequestBuilder<MediaPlayerWorker>()
            .setInputData(Data.Builder().putBoolean("isPlaying", false).build())
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun stopMediaPlayer() {
        timer?.cancel()
        meditationAnimation.stop()
        // Stop the background music
        sendRequestForMediaPlayer()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        sendRequestForMediaPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        sendRequestForMediaPlayer()
    }
}