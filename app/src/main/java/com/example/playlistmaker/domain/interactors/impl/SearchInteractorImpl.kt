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
        return if (query.isEmpty()) {
            searchHistoryRepository.getHistory().map { history ->
                if (history.isNotEmpty()) SearchInteractor.SearchResult.History(history)
                else SearchInteractor.SearchResult.EmptyHistory
            }
        } else {
            tracksRepository.searchTracks(query).map { tracks ->
                when {
                    tracks.isNotEmpty() -> SearchInteractor.SearchResult.Content(tracks)
                    else -> SearchInteractor.SearchResult.Empty
                }
            }.catch { e ->
                emit(SearchInteractor.SearchResult.Error("Ошибка поиска", e.message ?: "Неизвестная ошибка"))
            }
        }
    }

    override fun getSearchHistory(): Flow<List<Track>> {
        // Предполагаем, что searchHistoryRepository.getHistory() возвращает Flow
        return searchHistoryRepository.getHistory().map { history ->
            history.distinctBy { it.trackId }
        }
    }

    override suspend fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrack(track)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryRepository.clearHistory()
    }
}