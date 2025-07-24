package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.models.Track

interface SearchInteractor {
    suspend fun searchTracks(query: String): List<Track>
    suspend fun getSearchHistory(): List<Track>
    suspend fun addTrackToHistory(track: Track)
    suspend fun clearSearchHistory()
}