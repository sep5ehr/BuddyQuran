package com.example.persianquran.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.R

/**
 * Handles playing the application's entry audio («بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ»)
 * and exit audio («صَدَقَ اللَّهُ الْعَلِيُّ الْعَظِيمُ») cleanly using native Android MediaPlayer.
 *
 * It operates independently from QuranAudioPlayer so that Quran recitation, selected reciter,
 * audio quality, and playback positions are never altered or interrupted.
 */
class AppLifecycleAudioManager(context: Context) {

    companion object {
        private const val TAG = "AppLifecycleAudio"
    }

    private val appContext = context.applicationContext
    private var activePlayer: MediaPlayer? = null

    /**
     * Plays «بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ» when the application is opened/entered.
     */
    fun playEntryAudio() {
        playRawAudio(R.raw.audio_entry_bismillah, "Entry (Bismillah)")
    }

    /**
     * Plays «صَدَقَ اللَّهُ الْعَلِيُّ الْعَظِيمُ» when the user leaves/exits the application.
     */
    fun playExitAudio() {
        playRawAudio(R.raw.audio_exit_sadaqallah, "Exit (Sadaqallah)")
    }

    private fun playRawAudio(rawResId: Int, label: String) {
        try {
            // Stop any currently playing lifecycle sound to prevent overlap
            stopActivePlayer()

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                val afd = appContext.resources.openRawResourceFd(rawResId)
                if (afd == null) {
                    Log.w(TAG, "Raw resource descriptor not found for $label")
                    return
                }
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                setOnPreparedListener { mp ->
                    try {
                        mp.start()
                        Log.d(TAG, "Started playing $label audio successfully.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start $label audio: ${e.message}")
                        releasePlayer(mp)
                    }
                }

                setOnCompletionListener { mp ->
                    Log.d(TAG, "Finished playing $label audio.")
                    releasePlayer(mp)
                }

                setOnErrorListener { mp, what, extra ->
                    Log.w(TAG, "Error playing $label audio: what=$what, extra=$extra")
                    releasePlayer(mp)
                    true
                }

                prepareAsync()
            }

            activePlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing $label audio: ${e.message}", e)
        }
    }

    private fun releasePlayer(player: MediaPlayer) {
        try {
            if (activePlayer == player) {
                activePlayer = null
            }
            player.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing player: ${e.message}")
        }
    }

    private fun stopActivePlayer() {
        activePlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping active player: ${e.message}")
            } finally {
                try {
                    player.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing active player: ${e.message}")
                }
                activePlayer = null
            }
        }
    }

    fun release() {
        stopActivePlayer()
    }
}
