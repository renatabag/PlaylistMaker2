package com.example.playlistmaker.domain.interactors.impl

import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class SearchInteractorImpl(
    private val tracksRepository: TracksRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : SearchInteractor {

    override fun searchTracks(query: String): Flow<SearchInteractor.SearchResult> {
        return when {
            query.isEmpty() -> {
                // Показываем историю поиска
                searchHistoryRepository.getHistory()
                    .map { history ->
                        if (history.isNotEmpty()) {
                            SearchInteractor.SearchResult.History(history)
                        } else {
                            SearchInteractor.SearchResult.Empty
                        }
                    }
            }
            else -> {
                // Выполняем поиск
                tracksRepository.searchTracks(query)
                    .map { tracks ->
                        if (tracks.isEmpty()) {
                            SearchInteractor.SearchResult.Empty
                        } else {
                            SearchInteractor.SearchResult.Content(tracks)
                        }
                    }
                    .catch { e ->
                        emit(SearchInteractor.SearchResult.Error(e.message ?: "Неизвестная ошибка"))
                    }
            }
        }
    }

    override suspend fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrack(track)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryRepository.clearHistory()
    }
}