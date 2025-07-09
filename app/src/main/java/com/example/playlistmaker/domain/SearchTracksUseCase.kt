package com.example.playlistmaker.domain

import com.example.playlistmaker.domain.models.SearchState
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository
import kotlin.collections.isNotEmpty

class SearchTracksUseCase(
    private val tracksRepository: TracksRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) {
    suspend fun execute(query: String): SearchState {
        return if (query.isEmpty()) {
            val history = searchHistoryRepository.getHistory()
            if (history.isNotEmpty()) SearchState.History(history)
            else SearchState.Empty
        } else {
            try {
                val tracks = tracksRepository.searchTracks(query)
                if (tracks.isEmpty()) SearchState.Empty
                else SearchState.Content(tracks)
            } catch (e: Exception) {
                SearchState.Error("Ошибка", e.message ?: "Неизвестная ошибка")
            }
        }
    }
}