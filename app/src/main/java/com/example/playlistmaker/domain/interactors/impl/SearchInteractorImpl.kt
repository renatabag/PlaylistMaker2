package com.example.playlistmaker.domain.interactors.impl

import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.models.SearchState
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository

class SearchInteractorImpl(
    private val tracksRepository: TracksRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : SearchInteractor {

    override suspend fun searchTracks(query: String): SearchState {
        return try {
            if (query.isEmpty()) {
                val history = searchHistoryRepository.getHistory()
                if (history.isNotEmpty()) SearchState.History(history)
                else SearchState.Empty
            } else {
                val tracks = tracksRepository.searchTracks(query)
                if (tracks.isEmpty()) SearchState.Empty
                else SearchState.Content(tracks)
            }
        } catch (e: Exception) {
            SearchState.Error(e.message ?: "Неизвестная ошибка", e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getSearchHistory(): List<Track> {
        return searchHistoryRepository.getHistory()
    }

    override suspend fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrack(track)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryRepository.clearHistory()
    }

}