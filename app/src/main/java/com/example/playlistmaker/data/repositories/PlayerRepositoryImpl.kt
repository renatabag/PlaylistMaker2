package com.example.playlistmaker.data.repositories

import com.example.playlistmaker.data.player.MediaPlayerWrapper
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.repositories.PlayerRepository
import kotlinx.coroutines.flow.Flow

class PlayerRepositoryImpl(
    private val mediaPlayer: MediaPlayerWrapper
) : PlayerRepository {

    override fun prepare(url: String): Flow<PlayerState> {
        return mediaPlayer.prepare(url)
    }

    override fun play() {
        mediaPlayer.play()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun getCurrentPosition(): Long {
        return mediaPlayer.getCurrentPosition()
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying()
    }
}
