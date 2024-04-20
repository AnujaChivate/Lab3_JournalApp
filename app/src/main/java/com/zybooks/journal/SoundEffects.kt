package com.zybooks.journal

import android.content.Context
import android.media.SoundPool
import android.media.AudioAttributes

class SoundEffects private constructor(context: Context){

    private var soundPool: SoundPool? = null
    private val selectSoundIds = mutableListOf<Int>()
    private var soundIndex = 0

    companion object {
        private var instance: SoundEffects? = null

        fun getInstance(context: Context): SoundEffects {
            if (instance == null) {
                instance = SoundEffects(context)
            }
            return instance!!
        }
    }

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build()

        soundPool?.let {
            selectSoundIds.add(it.load(context, R.raw.journal_created, 1))
            selectSoundIds.add(it.load(context, R.raw.journal_deleted, 1))
        }

        resetTones()
    }

    private fun resetTones() {
        soundIndex = -1
    }

    fun playJournalCreated() {
        soundPool?.play(selectSoundIds[0], 1f, 1f, 1, 0, 1f)
    }

    fun playJournalDeleted() {
        soundPool?.play(selectSoundIds[1], 0.5f, 0.5f, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}