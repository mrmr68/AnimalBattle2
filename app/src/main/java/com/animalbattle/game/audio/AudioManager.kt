package com.animalbattle.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class AudioManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private var soundEnabled = true
    private var musicEnabled = true

    private val soundMap = mutableMapOf<SoundType, Int>()

    enum class SoundType {
        BUTTON_CLICK,
        COIN_EARN,
        TROPHY_EARN,
        LEVEL_UP,
        ATTACK,
        DAMAGE,
        VICTORY,
        DEFEAT,
        SPIN_START,
        SPIN_END,
        CHEST_OPEN,
        ABILITY_USE,
        QUESTION_CORRECT,
        QUESTION_WRONG,
        TIMER_TICK
    }

    fun initialize() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
    }

    fun playSound(soundType: SoundType) {
        if (!soundEnabled) return
        soundMap[soundType]?.let { soundId ->
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun loadSound(soundType: SoundType, resourceId: Int) {
        soundPool?.let { pool ->
            val soundId = pool.load(context, resourceId, 1)
            soundMap[soundType] = soundId
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
