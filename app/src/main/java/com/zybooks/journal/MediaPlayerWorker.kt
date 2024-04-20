package com.zybooks.journal

import android.content.Context
import android.media.MediaPlayer
import androidx.work.Worker
import androidx.work.WorkerParameters

class MediaPlayerWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private var mediaPlayer: MediaPlayer? = null

    override fun doWork(): Result {
        val isPlaying = inputData.getBoolean("isPlaying", false)

        if (isPlaying) {
            startMediaPlayer()
        } else {
            stopMediaPlayer()
        }

        return Result.success()
    }

    private fun startMediaPlayer() {
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.meditation)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}