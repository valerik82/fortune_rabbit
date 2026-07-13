package com.rabbitsluckandfortuneppamobs.audio

import android.content.Context
import android.media.AudioManager as SystemAudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Lightweight audio + haptics helper (spec §8 Animations feedback, §9 Sounds).
 *
 * Sound effects are generated with [ToneGenerator] so the MVP ships without
 * bundled audio files. Looped background music will play automatically if a
 * `res/raw/bg_music` track is added to the project; until then it is a safe
 * no-op. Every channel respects the player's Settings toggles.
 */
class AudioManager(private val context: Context) {

    enum class Sfx { FLIP, MATCH, MISMATCH, LEVEL_COMPLETE, COIN, TAP }

    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true
    var vibrationEnabled: Boolean = true

    private var toneGen: ToneGenerator? = null
    private var musicPlayer: MediaPlayer? = null

    private fun tone(): ToneGenerator? {
        if (toneGen == null) {
            toneGen = runCatching {
                ToneGenerator(SystemAudioManager.STREAM_MUSIC, 80)
            }.getOrNull()
        }
        return toneGen
    }

    fun play(sfx: Sfx) {
        if (!soundEnabled) return
        val toneType = when (sfx) {
            Sfx.FLIP -> ToneGenerator.TONE_PROP_BEEP
            Sfx.MATCH -> ToneGenerator.TONE_PROP_ACK
            Sfx.MISMATCH -> ToneGenerator.TONE_PROP_NACK
            Sfx.LEVEL_COMPLETE -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            Sfx.COIN -> ToneGenerator.TONE_PROP_BEEP2
            Sfx.TAP -> ToneGenerator.TONE_PROP_PROMPT
        }
        val duration = if (sfx == Sfx.LEVEL_COMPLETE) 400 else 120
        runCatching { tone()?.startTone(toneType, duration) }
    }

    fun vibrate(millis: Long = 30) {
        if (!vibrationEnabled) return
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        }
    }

    // ---- Background music (optional asset) -------------------------------

    fun startMusic() {
        if (!musicEnabled) return
        // Look up an optional bundled loop; absent by default in the MVP.
        val resId = context.resources.getIdentifier("bg_music", "raw", context.packageName)
        if (resId == 0) return
        if (musicPlayer == null) {
            musicPlayer = runCatching {
                MediaPlayer.create(context, resId)?.apply {
                    isLooping = true
                    setVolume(0.4f, 0.4f)
                }
            }.getOrNull()
        }
        runCatching { musicPlayer?.takeIf { !it.isPlaying }?.start() }
    }

    fun stopMusic() {
        runCatching { musicPlayer?.takeIf { it.isPlaying }?.pause() }
    }

    fun applySettings(sound: Boolean, music: Boolean, vibration: Boolean) {
        soundEnabled = sound
        vibrationEnabled = vibration
        val musicWasOn = musicEnabled
        musicEnabled = music
        if (music && !musicWasOn) startMusic()
        if (!music && musicWasOn) stopMusic()
    }

    fun release() {
        runCatching { toneGen?.release() }
        toneGen = null
        runCatching { musicPlayer?.release() }
        musicPlayer = null
    }
}
