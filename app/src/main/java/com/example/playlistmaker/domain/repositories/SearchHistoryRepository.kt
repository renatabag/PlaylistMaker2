package com.example.playlistmaker.domain.repositories

import com.example.playlistmaker.data.dto.TrackDTO
import com.example.playlistmaker.domain.models.Track

interface SearchHistoryRepository {
    suspend fun getHistory(): List<Track>
    suspend fun addTrack(track: Track)
    suspend fun clearHistory()
}