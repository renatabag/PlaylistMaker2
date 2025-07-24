package com.example.playlistmaker.domain

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchTracksUseCase(
    private val tracksRepository: TracksRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) {
    fun search(query: String): Flow<Result<List<Track>>> = flow {
        try {
            if (query.isEmpty()) {
                val history = searchHistoryRepository.getHistory()
                emit(Result.success(history))
            } else {
                val tracks = tracksRepository.searchTracks(query)
                emit(Result.success(tracks))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun addToHistory(track: Track) {
        searchHistoryRepository.addTrack(track)
    }

    suspend fun getHistory(): List<Track> {
        return searchHistoryRepository.getHistory()
    }

    suspend fun clearHistory() {
        searchHistoryRepository.clearHistory()
    }
}