package com.example.playlistmaker.domain.repositories

import com.example.playlistmaker.domain.models.PlayerState
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun prepare(url: String): Flow<PlayerState>
    fun play()
    fun pause()
    fun release()
    fun getCurrentPosition(): Long
    fun isPlaying(): Boolean
}