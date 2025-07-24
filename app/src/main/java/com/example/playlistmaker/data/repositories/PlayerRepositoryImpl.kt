package com.example.playlistmaker.data.repositories

import android.media.MediaPlayer
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.repositories.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class PlayerRepositoryImpl(
    private val mediaPlayer: MediaPlayer,
    private val coroutineScope: CoroutineScope
) : PlayerRepository {

    private val playerStateFlow = MutableSharedFlow<PlayerState>()

    override fun prepare(url: String): Flow<PlayerState> {
        mediaPlayer.apply {
            reset()
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                coroutineScope.launch {
                    playerStateFlow.emit(PlayerState.Prepared)
                }
            }
            setOnCompletionListener {
                coroutineScope.launch {
                    playerStateFlow.emit(PlayerState.Complete)
                }
            }
            setOnErrorListener { _, _, _ ->
                coroutineScope.launch {
                    playerStateFlow.emit(PlayerState.Error("Ошибка воспроизведения"))
                }
                true
            }
        }
        return playerStateFlow
    }

    override fun play() {
        mediaPlayer.start()
        coroutineScope.launch {
            playerStateFlow.emit(PlayerState.Playing)
        }
    }

    override fun pause() {
        mediaPlayer.pause()
        coroutineScope.launch {
            playerStateFlow.emit(PlayerState.Paused)
        }
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun getCurrentPosition(): Long = mediaPlayer.currentPosition.toLong()

    override fun isPlaying(): Boolean = mediaPlayer.isPlaying
}