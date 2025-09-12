package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    suspend fun addTrackToHistory(track: Track)
    suspend fun clearSearchHistory()
    fun searchTracks(query: String): Flow<SearchResult>

    sealed class SearchResult {
        data class Content(val tracks: List<Track>) : SearchResult()
        object Empty : SearchResult()
        data class EmptyError(val message: String) : SearchResult()
        data class Error(val message: String) : SearchResult()
        data class History(val tracks: List<Track>) : SearchResult()
    }
}