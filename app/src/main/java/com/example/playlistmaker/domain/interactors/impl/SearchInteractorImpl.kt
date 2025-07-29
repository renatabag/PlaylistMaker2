package com.example.playlistmaker.domain.interactors.impl

import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository

class SearchInteractorImpl(
    private val tracksRepository: TracksRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : SearchInteractor {

    override suspend fun searchTracks(query: String): SearchInteractor.SearchResult {
        return try {
            when {
                query.isEmpty() -> {
                    val history = getSearchHistory()
                    if (history.isNotEmpty()) SearchInteractor.SearchResult.History(history)
                    else SearchInteractor.SearchResult.Empty
                }
                else -> {
                    val tracks = tracksRepository.searchTracks(query)
                    when {
                        tracks.isEmpty() -> SearchInteractor.SearchResult.Empty
                        else -> SearchInteractor.SearchResult.Content(tracks)
                    }
                }
            }
        } catch (e: Exception) {
            SearchInteractor.SearchResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    override suspend fun getSearchHistory(): List<Track> {
        return searchHistoryRepository.getHistory().distinctBy { it.trackId }
    }

    override suspend fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrack(track)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryRepository.clearHistory()
    }
}