package com.example.playlistmaker.data.player

import android.media.MediaPlayer
import com.example.playlistmaker.domain.models.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class MediaPlayerWrapper {

    private var mediaPlayer: MediaPlayer? = null
    private val playerStateFlow = MutableSharedFlow<PlayerState>()

    fun prepare(url: String): Flow<PlayerState> {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                playerStateFlow.tryEmit(PlayerState.Prepared)
            }
            setOnCompletionListener {
                playerStateFlow.tryEmit(PlayerState.Complete)
            }
            setOnErrorListener { _, _, _ ->
                playerStateFlow.tryEmit(PlayerState.Error("Ошибка воспроизведения"))
                true
            }
        }
        return playerStateFlow
    }

    fun play() {
        mediaPlayer?.start()
        playerStateFlow.tryEmit(PlayerState.Playing)
    }

    fun pause() {
        mediaPlayer?.pause()
        playerStateFlow.tryEmit(PlayerState.Paused)
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun getCurrentPosition(): Long {
        return mediaPlayer?.currentPosition?.toLong() ?: 0L
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}