package com.example.playlistmaker.domain

import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository
import com.example.playlistmaker.presentation.mappers.TrackUiMapper
import com.example.playlistmaker.presentation.ui.states.SearchState
import kotlin.collections.isNotEmpty

class SearchTracksUseCase(
    private val tracksRepository: TracksRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val trackUiMapper: TrackUiMapper
) {
    suspend fun execute(query: String): SearchState {
        return if (query.isEmpty()) {
            val history = searchHistoryRepository.getHistory()
            if (history.isNotEmpty()) SearchState.History(trackUiMapper.mapListToUi(history))
            else SearchState.EmptyHistory
        } else {
            try {
                val tracks = tracksRepository.searchTracks(query)
                if (tracks.isEmpty()) SearchState.Empty
                else SearchState.Content(trackUiMapper.mapListToUi(tracks))
            } catch (e: Exception) {
                SearchState.Error("Ошибка", e.message ?: "Неизвестная ошибка")
            }
        }
    }
}