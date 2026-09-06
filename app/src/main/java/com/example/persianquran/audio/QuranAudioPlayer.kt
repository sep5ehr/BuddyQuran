package com.example.persianquran.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import com.example.persianquran.data.model.AudioTrackState
import com.example.persianquran.data.model.Verse
import com.example.persianquran.data.repository.QuranRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class QuranAudioPlayer(
    private val context: Context,
    private val repository: QuranRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "QuranAudioPlayer"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _playerState = MutableStateFlow(AudioTrackState())
    val playerState: StateFlow<AudioTrackState> = _playerState.asStateFlow()

    private var playlist: List<Verse> = emptyList()
    private var currentIndex: Int = -1
    private var currentReciterId: Int = 7
    private var currentReciterName: String = "مشاری راشد العفاسی"
    private var autoAdvance: Boolean = true
    private var repeatAyah: Boolean = false
    private var playbackSpeed: Float = 1.0f

    fun configureSettings(
        reciterId: Int,
        reciterName: String,
        autoAdvance: Boolean,
        repeatAyah: Boolean,
        speed: Float
    ) {
        this.currentReciterId = reciterId
        this.currentReciterName = reciterName
        this.autoAdvance = autoAdvance
        this.repeatAyah = repeatAyah
        this.playbackSpeed = speed

        if (mediaPlayer != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = mediaPlayer?.playbackParams ?: PlaybackParams()
                params.speed = speed
                mediaPlayer?.playbackParams = params
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update playback speed: ${e.message}")
            }
        }
    }

    fun playVerseList(verses: List<Verse>, startIndex: Int, reciterName: String = currentReciterName) {
        if (verses.isEmpty() || startIndex !in verses.indices) return
        this.playlist = verses
        this.currentReciterName = reciterName
        playIndex(startIndex)
    }

    fun playIndex(index: Int) {
        if (index !in playlist.indices) return
        currentIndex = index
        val verse = playlist[index]
        playVerse(verse)
    }

    fun playVerse(verse: Verse) {
        stopProgressTracker()
        releasePlayer()

        _playerState.value = _playerState.value.copy(
            isBuffering = true,
            isPlaying = false,
            currentSurahId = verse.chapterId,
            currentVerseNumber = verse.verseNumber,
            currentVerseKey = verse.verseKey,
            reciterName = currentReciterName,
            currentPositionMs = 0,
            durationMs = 0,
            errorMessage = null
        )

        scope.launch(Dispatchers.IO) {
            try {
                // Check if downloaded locally first
                val localPath = repository.getDownloadedAudioPath(verse.verseKey, currentReciterId)

                if (localPath == null && !isOnline(context)) {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        errorMessage = "برای پخش این تلاوت، اتصال به اینترنت لازم است."
                    )
                    return@launch
                }

                val audioSource = if (localPath != null) {
                    Log.d(TAG, "Playing from local file: $localPath")
                    localPath
                } else {
                    verse.audioUrl
                }

                if (audioSource.isNullOrBlank()) {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        errorMessage = "آدرس صوت برای این آیه یافت نشد"
                    )
                    return@launch
                }

                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(audioSource)
                    setOnPreparedListener { mp ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                val params = mp.playbackParams
                                params.speed = playbackSpeed
                                mp.playbackParams = params
                            } catch (e: Exception) {
                                Log.w(TAG, "Error setting speed: ${e.message}")
                            }
                        }
                        mp.start()
                        _playerState.value = _playerState.value.copy(
                            isBuffering = false,
                            isPlaying = true,
                            durationMs = mp.duration
                        )
                        startProgressTracker()
                    }

                    setOnCompletionListener {
                        onTrackFinished()
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                        val err = if (!isOnline(context)) {
                            "برای پخش این تلاوت، اتصال به اینترنت لازم است."
                        } else {
                            "خطا در پخش صوت آیه"
                        }
                        _playerState.value = _playerState.value.copy(
                            isBuffering = false,
                            isPlaying = false,
                            errorMessage = err
                        )
                        true
                    }

                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting playback: ${e.message}", e)
                val err = if (!isOnline(context)) {
                    "برای پخش این تلاوت، اتصال به اینترنت لازم است."
                } else {
                    "خطا در اتصال به سرور صوت"
                }
                _playerState.value = _playerState.value.copy(
                    isBuffering = false,
                    isPlaying = false,
                    errorMessage = err
                )
            }
        }
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun onTrackFinished() {
        stopProgressTracker()
        if (repeatAyah && currentIndex in playlist.indices) {
            playIndex(currentIndex)
        } else if (autoAdvance && currentIndex + 1 in playlist.indices) {
            playIndex(currentIndex + 1)
        } else {
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isBuffering = false,
                currentPositionMs = _playerState.value.durationMs
            )
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
                stopProgressTracker()
                _playerState.value = _playerState.value.copy(isPlaying = false)
            } else {
                mp.start()
                startProgressTracker()
                _playerState.value = _playerState.value.copy(isPlaying = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Toggle play/pause failed: ${e.message}")
        }
    }

    fun pause() {
        val mp = mediaPlayer ?: return
        try {
            if (mp.isPlaying) {
                mp.pause()
                stopProgressTracker()
                _playerState.value = _playerState.value.copy(isPlaying = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pause failed: ${e.message}")
        }
    }

    fun resume() {
        val mp = mediaPlayer ?: return
        try {
            if (!mp.isPlaying) {
                mp.start()
                startProgressTracker()
                _playerState.value = _playerState.value.copy(isPlaying = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Resume failed: ${e.message}")
        }
    }

    fun playNext() {
        if (currentIndex + 1 in playlist.indices) {
            playIndex(currentIndex + 1)
        }
    }

    fun playPrevious() {
        if (currentIndex - 1 in playlist.indices) {
            playIndex(currentIndex - 1)
        }
    }

    fun seekTo(positionMs: Int) {
        val mp = mediaPlayer ?: return
        try {
            mp.seekTo(positionMs)
            _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            Log.e(TAG, "Seek failed: ${e.message}")
        }
    }

    fun stop() {
        stopProgressTracker()
        releasePlayer()
        _playerState.value = AudioTrackState()
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        try {
                            _playerState.value = _playerState.value.copy(
                                currentPositionMs = mp.currentPosition,
                                durationMs = mp.duration
                            )
                        } catch (_: Exception) {}
                    }
                }
                delay(300)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
