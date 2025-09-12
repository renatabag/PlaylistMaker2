package com.example.playlistmaker.domain

import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository
import com.example.playlistmaker.presentation.mappers.TrackUiMapper
import com.example.playlistmaker.presentation.ui.states.SearchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SearchTracksUseCase(
    private val tracksRepository: TracksRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val trackUiMapper: TrackUiMapper
) {
    fun execute(query: String): Flow<SearchState> {
        return if (query.isEmpty()) {
            searchHistoryRepository.getHistory().map { history ->
                if (history.isNotEmpty()) SearchState.History(trackUiMapper.mapListToUi(history))
                else SearchState.EmptyHistory
            }
        } else {
            tracksRepository.searchTracks(query).map { tracks ->
                if (tracks.isEmpty()) SearchState.Empty
                else SearchState.Content(trackUiMapper.mapListToUi(tracks))
            }
        }
    }
}