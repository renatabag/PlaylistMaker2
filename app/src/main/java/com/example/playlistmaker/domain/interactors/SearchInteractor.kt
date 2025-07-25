package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.models.SearchState

interface SearchInteractor {
    suspend fun searchTracks(query: String): SearchState
    suspend fun getSearchHistory(): List<com.example.playlistmaker.domain.models.Track>
    suspend fun addTrackToHistory(track: com.example.playlistmaker.domain.models.Track)
    suspend fun clearSearchHistory()
}

