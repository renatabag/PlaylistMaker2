package com.example.playlistmaker.data.repositories

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.playlistmaker.presentation.ui.states.PlayerState
import com.example.playlistmaker.domain.repositories.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class PlayerRepositoryImpl (
    private val coroutineScope: CoroutineScope
) : PlayerRepository {
    private var mediaPlayer: MediaPlayer? = null
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default)
    private val playerStateFlow = _playerState.asStateFlow()

    private var progressJob: Job? = null
    private var isTracking = false

    private fun setupMediaPlayerListeners() {
        mediaPlayer?.apply {
            setOnPreparedListener {
                _playerState.value = PlayerState.Prepared(0L)
                startProgressUpdates()
            }
            setOnCompletionListener {
                stopProgressUpdates()
                _playerState.value = PlayerState.Complete(0L)
            }
            setOnErrorListener { _, what, extra ->
                stopProgressUpdates()
                _playerState.value = PlayerState.Error(what.toString(), extra, currentPosition.toLong())
                true
            }
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates() // Останавливаем предыдущие обновления

        progressJob = coroutineScope.launch {
            while (mediaPlayer?.isPlaying == true && !isTracking) {
                val currentPosition = mediaPlayer?.currentPosition?.toLong() ?: 0L
                _playerState.value = PlayerState.Progress(currentPosition)
                delay(1000) // Обновляем каждую секунду
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun prepare(url: String): Flow<PlayerState> = flow {
        try {
            mediaPlayer?.release()
            mediaPlayer = null

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setOnPreparedListener {
                    _playerState.value = PlayerState.Prepared(0L)
                }
                setOnCompletionListener {
                    stopProgressUpdates()
                    _playerState.value = PlayerState.Complete(0L)
                }
                setOnErrorListener { _, what, extra ->
                    stopProgressUpdates()
                    _playerState.value = PlayerState.Error(what.toString(), extra, currentPosition.toLong())
                    true
                }

                setDataSource(url)
                prepareAsync()
            }

            playerStateFlow.collect { state ->
                emit(state)
            }
        } catch (e: Exception) {
            _playerState.value = PlayerState.Error(e.message ?: "Unknown error", 0, 0L)
            emit(PlayerState.Error(e.message ?: "Unknown error", 0, 0L))
        }
    }

    override fun play() {
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.start()
                _playerState.value = PlayerState.Playing(mp.currentPosition.toLong())
                startProgressUpdates()
            }
        }
    }

    override fun pause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                stopProgressUpdates()
                _playerState.value = PlayerState.Paused(mp.currentPosition.toLong())
            }
        }
    }

    override fun release() {
        stopProgressUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
        _playerState.value = PlayerState.Default
    }

    override fun getCurrentPosition(): Long {
        return mediaPlayer?.currentPosition?.toLong() ?: 0L
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    override fun playbackControl() {
        if (isPlaying()) {
            pause()
        } else {
            play()
        }
    }

    override fun seekTo(position: Long) {
        isTracking = true
        mediaPlayer?.seekTo(position.toInt())
        _playerState.value = PlayerState.Progress(position)

        // После завершения перемотки возобновляем обновления
        coroutineScope.launch {
            delay(300)
            isTracking = false
            if (mediaPlayer?.isPlaying == true) {
                startProgressUpdates()
            }
        }
    }
}