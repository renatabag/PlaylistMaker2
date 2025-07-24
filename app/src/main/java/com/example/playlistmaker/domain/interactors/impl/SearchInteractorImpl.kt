package com.example.playlistmaker.domain.interactors.impl

import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository

class SearchInteractorImpl(
    private val tracksRepository: TracksRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : SearchInteractor {

    override suspend fun searchTracks(query: String): List<Track> {
        return if (query.isEmpty()) {
            searchHistoryRepository.getHistory()
        } else {
            try {
                tracksRepository.searchTracks(query)
            } catch (e: Exception) {
                emptyList()
            }
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