package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.models.SearchState
import com.example.playlistmaker.domain.models.Track

interface SearchInteractor {
    sealed class SearchResult {
        data class Content(val tracks: List<Track>) : SearchResult()
        object Empty : SearchResult()
        data class Error(val message: String) : SearchResult()
        data class History(val tracks: List<Track>) : SearchResult()
    }

    suspend fun searchTracks(query: String): SearchState
    suspend fun addTrackToHistory(track: Track)
    suspend fun clearSearchHistory()
    suspend fun getSearchHistory(): List<Track>
}