package com.example.playlistmaker.domain.repositories.impl

import android.media.MediaPlayer
import com.example.playlistmaker.presentation.ui.states.PlayerState
import com.example.playlistmaker.domain.repositories.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerRepositoryImpl(
    private val mediaPlayer: MediaPlayer
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

    override fun prepare(url: String): Flow<PlayerState> {
        mediaPlayer.apply {
            reset()
            setDataSource(url)
            prepareAsync()
        }
        return playerStateFlow
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