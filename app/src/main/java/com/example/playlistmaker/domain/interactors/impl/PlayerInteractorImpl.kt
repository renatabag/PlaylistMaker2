package com.example.playlistmaker.domain.interactors.impl

import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.repositories.PlayerRepository
import kotlinx.coroutines.flow.Flow

class PlayerInteractorImpl(
    private val playerRepository: PlayerRepository
) : PlayerInteractor {

    override fun prepare(url: String): Flow<PlayerState> {
        return playerRepository.prepare(url)
    }

    override fun play() {
        playerRepository.play()
    }

    override fun pause() {
        playerRepository.pause()
    }

    override fun release() {
        playerRepository.release()
    }

    override fun getCurrentPosition(): Long {
        return playerRepository.getCurrentPosition()
    }

    override fun isPlaying(): Boolean {
        return playerRepository.isPlaying()
    }
}