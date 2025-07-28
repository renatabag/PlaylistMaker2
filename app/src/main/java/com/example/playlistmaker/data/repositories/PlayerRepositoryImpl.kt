package com.example.playlistmaker.domain.repositories.impl

import android.media.MediaPlayer
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.repositories.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerRepositoryImpl : PlayerRepository {
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default)
    private val playerStateFlow = _playerState.asStateFlow()

    init {
        setupMediaPlayerListeners()
    }

    private fun setupMediaPlayerListeners() {
        mediaPlayer.apply {
            setOnPreparedListener {
                _playerState.value = PlayerState.Prepared
            }
            setOnCompletionListener {
                _playerState.value = PlayerState.Prepared
            }
            setOnErrorListener { _, what, extra ->
                _playerState.value = PlayerState.Error(what.toString(), extra)
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
        _playerState.value = PlayerState.Playing
    }

    override fun pause() {
        mediaPlayer.pause()
        _playerState.value = PlayerState.Paused
    }

    override fun release() {
        mediaPlayer.release()
        _playerState.value = PlayerState.Default
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