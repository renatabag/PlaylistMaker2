package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    fun searchTracks(query: String): Flow<SearchResult>
    suspend fun addTrackToHistory(track: Track)
    fun getSearchHistory(): Flow<List<Track>>
    suspend fun clearSearchHistory()

    sealed interface SearchResult {
        data class Content(val tracks: List<Track>) : SearchResult
        data class History(val tracks: List<Track>) : SearchResult
        data class Error(val message: String, val string: String) : SearchResult
        object Empty : SearchResult
        object EmptyHistory : SearchResult
    }
}