package com.example.playlistmaker.domain.repositories

import com.example.playlistmaker.data.dto.TrackDTO
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getHistory(): Flow<List<Track>>
    suspend fun addTrack(track: Track)
    suspend fun clearHistory()
}