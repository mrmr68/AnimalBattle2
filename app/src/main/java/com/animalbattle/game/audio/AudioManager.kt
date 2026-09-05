package com.animalbattle.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager as AndroidAudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import kotlin.math.ln

/**
 * Enhanced audio manager with volume controls, sound effects,
 * background music, and audio focus handling.
 */
class AudioManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private var soundEnabled = true
    private var musicEnabled = true
    private var soundVolume = 1f
    private var musicVolume = 0.7f
    private var isInitialized = false
    private var currentMusicResId: Int? = null

    private val soundMap = mutableMapOf<SoundType, Int>()
    private val loadedSoundIds = mutableSetOf<Int>()

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
        TIMER_TICK,
        // New sounds
        COMBO,
        BOOST,
        PERFECT,
        PAUSE,
        UNLOCK,
        PAGE_TURN,
        WHOOSH,
        DRUM_ROLL
    }

    fun initialize() {
        if (isInitialized) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build()

            isInitialized = true
        } catch (e: Exception) {
            // Graceful degradation if audio init fails
            isInitialized = false
        }
    }

    // ── Volume Controls ──────────────────────────────────

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

    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(musicVolume, musicVolume)
    }

    fun getSoundVolume(): Float = soundVolume
    fun getMusicVolume(): Float = musicVolume
    fun isSoundEnabled(): Boolean = soundEnabled
    fun isMusicEnabled(): Boolean = musicEnabled

    // ── Sound Effects ────────────────────────────────────

    fun playSound(soundType: SoundType) {
        if (!soundEnabled || !isInitialized) return
        soundMap[soundType]?.let { soundId ->
            try {
                soundPool?.play(soundId, soundVolume, soundVolume, 1, 0, 1f)
            } catch (_: Exception) { /* ignore */ }
        }
    }

    fun playSoundWithPitch(soundType: SoundType, pitch: Float) {
        if (!soundEnabled || !isInitialized) return
        soundMap[soundType]?.let { soundId ->
            try {
                soundPool?.play(soundId, soundVolume, soundVolume, 1, 0, pitch.coerceIn(0.5f, 2f))
            } catch (_: Exception) { /* ignore */ }
        }
    }

    fun loadSound(soundType: SoundType, resourceId: Int) {
        if (!isInitialized) return
        try {
            soundPool?.let { pool ->
                val soundId = pool.load(context, resourceId, 1)
                soundMap[soundType] = soundId
                loadedSoundIds.add(soundId)
            }
        } catch (_: Exception) { /* ignore */ }
    }

    fun preloadSounds(sounds: Map<SoundType, Int>) {
        sounds.forEach { (type, resId) -> loadSound(type, resId) }
    }

    // ── Background Music ─────────────────────────────────

    fun playMusic(resourceId: Int, loop: Boolean = true) {
        if (!musicEnabled) return
        if (currentMusicResId == resourceId && mediaPlayer?.isPlaying == true) return

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resourceId)?.apply {
                isLooping = loop
                setVolume(musicVolume, musicVolume)
                start()
            }
            currentMusicResId = resourceId
        } catch (_: Exception) { /* ignore */ }
    }

    fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) { /* ignore */ }
    }

    fun resumeMusic() {
        if (!musicEnabled) return
        try {
            mediaPlayer?.start()
        } catch (_: Exception) { /* ignore */ }
    }

    fun stopMusic() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
            currentMusicResId = null
        } catch (_: Exception) { /* ignore */ }
    }

    fun fadeInMusic(resourceId: Int, durationMs: Int = 1000) {
        if (!musicEnabled) return
        playMusic(resourceId)
        // Fade in by gradually increasing volume
        val steps = 20
        val stepDelay = durationMs.toLong() / steps
        val volumeStep = musicVolume / steps
        for (i in 1..steps) {
            android.os.Handler(context.mainLooper).postDelayed({
                mediaPlayer?.setVolume(volumeStep * i, volumeStep * i)
            }, stepDelay * i)
        }
    }

    fun fadeOutMusic(durationMs: Int = 1000) {
        val steps = 20
        val stepDelay = durationMs.toLong() / steps
        val currentVol = musicVolume
        val volumeStep = currentVol / steps
        for (i in 1..steps) {
            android.os.Handler(context.mainLooper).postDelayed({
                mediaPlayer?.setVolume(currentVol - volumeStep * i, currentVol - volumeStep * i)
                if (i == steps) {
                    pauseMusic()
                    mediaPlayer?.setVolume(currentVol, currentVol)
                }
            }, stepDelay * i)
        }
    }

    // ── Cleanup ──────────────────────────────────────────

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            mediaPlayer?.release()
            mediaPlayer = null
            loadedSoundIds.clear()
            soundMap.clear()
            isInitialized = false
            currentMusicResId = null
        } catch (_: Exception) { /* ignore */ }
    }

    fun onPause() {
        pauseMusic()
    }

    fun onResume() {
        resumeMusic()
    }
}
