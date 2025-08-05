package com.example.playlistmaker.data.repositories

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.playlistmaker.presentation.ui.states.PlayerState
import com.example.playlistmaker.domain.repositories.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class PlayerRepositoryImpl(
    private var mediaPlayer: MediaPlayer
) : PlayerRepository {
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default(0L))
    private val playerStateFlow = _playerState.asStateFlow()

    init {
        setupMediaPlayerListeners()
    }

    private fun setupMediaPlayerListeners() {
        mediaPlayer.apply {
            setOnPreparedListener {
                _playerState.value = PlayerState.Prepared(0L)
            }
            setOnCompletionListener {
                _playerState.value = PlayerState.Prepared(mediaPlayer.currentPosition.toLong())
            }
            setOnErrorListener { _, what, extra ->
                _playerState.value = PlayerState.Error(what.toString(), extra, mediaPlayer.currentPosition.toLong())
                true
            }
        }
    }

    override fun prepare(url: String): Flow<PlayerState> = flow {
        try {
            mediaPlayer?.release()

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
                    _playerState.value = PlayerState.Prepared(currentPosition.toLong())
                }
                setOnErrorListener { _, what, extra ->
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
        mediaPlayer.start()
        _playerState.value = PlayerState.Playing(mediaPlayer.currentPosition.toLong())
    }

    override fun pause() {
        mediaPlayer.pause()
        _playerState.value = PlayerState.Paused(mediaPlayer.currentPosition.toLong())
    }

    override fun release() {
        mediaPlayer.release()
        _playerState.value = PlayerState.Default(0L)
    }

    override fun getCurrentPosition(): Long {
        return mediaPlayer.currentPosition.toLong()
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun playbackControl() {
        if (mediaPlayer.isPlaying) {
            pause()
        } else {
            play()
        }
    }
}