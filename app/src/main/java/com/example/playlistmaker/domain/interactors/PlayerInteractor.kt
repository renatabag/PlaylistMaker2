package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.presentation.ui.states.PlayerState
import kotlinx.coroutines.flow.Flow

interface PlayerInteractor {
    fun prepare(url: String): Flow<PlayerState>
    fun play()
    fun pause()
    fun release()
    fun getCurrentPosition(): Long
    fun isPlaying(): Boolean
    fun playbackControl()
}