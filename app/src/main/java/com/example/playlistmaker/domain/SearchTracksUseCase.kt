package com.example.playlistmaker.domain

import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository
import com.example.playlistmaker.presentation.mappers.TrackUiMapper
import com.example.playlistmaker.presentation.ui.states.SearchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class SearchTracksUseCase(
    private val tracksRepository: TracksRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val trackUiMapper: TrackUiMapper
) {
    fun execute(query: String): Flow<SearchState> {
        return when {
            query.isEmpty() -> {
                // Показываем историю поиска
                searchHistoryRepository.getHistory()
                    .map { history ->
                        if (history.isNotEmpty()) {
                            SearchState.History(trackUiMapper.mapListToUi(history))
                        } else {
                            SearchState.EmptyHistory
                        }
                    }
                    .onStart {
                        // Можно показать состояние загрузки для истории
                        emit(SearchState.EmptyHistory)
                    }
            }
            else -> {
                // Выполняем поиск
                tracksRepository.searchTracks(query)
                    .map { tracks ->
                        if (tracks.isEmpty()) {
                            SearchState.Empty
                        } else {
                            SearchState.Content(trackUiMapper.mapListToUi(tracks))
                        }
                    }
                    .onStart {
                        // Показываем состояние загрузки
                        emit(SearchState.Loading)
                    }
                    .catch { e ->
                        // Обрабатываем ошибки - теперь только один параметр!
                        emit(SearchState.Error(e.message ?: "Неизвестная ошибка",e.message ?: "Неизвестная ошибка"))
                    }
            }
        }
    }
}