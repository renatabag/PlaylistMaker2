package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.mappers.TrackUiMapper
import com.example.playlistmaker.presentation.ui.states.SearchState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val trackUiMapper: TrackUiMapper
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Empty)
    val searchState: StateFlow<SearchState> = _searchState

    private var searchJob: Job? = null
    private var isHistoryLoaded = false

    fun searchTracks(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            if (!isHistoryLoaded) {
                loadSearchHistory()
            }
            return
        }

        _searchState.value = SearchState.Loading
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)

            searchInteractor.searchTracks(query).collect { result ->
                _searchState.value = when (result) {
                    is SearchInteractor.SearchResult.Content ->
                        SearchState.Content(trackUiMapper.mapListToUi(result.tracks))
                    is SearchInteractor.SearchResult.History ->
                        SearchState.History(trackUiMapper.mapListToUi(result.tracks))
                    is SearchInteractor.SearchResult.Error ->
                        SearchState.Error(
                            result.message ?: "Неизвестная ошибка",
                            result.message ?: "Неизвестная ошибка"
                        )
                    SearchInteractor.SearchResult.Empty -> SearchState.Empty
                    SearchInteractor.SearchResult.EmptyHistory -> SearchState.EmptyHistory
                }
            }
        }
    }

    private fun loadSearchHistory() {
        searchJob = viewModelScope.launch {
            val history = searchInteractor.getSearchHistory().first()
            isHistoryLoaded = true
            if (history.isEmpty()) {
                _searchState.value = SearchState.EmptyHistory
            } else {
                _searchState.value = SearchState.History(trackUiMapper.mapListToUi(history))
            }
        }
    }

    fun addTrackToHistory(track: Track) {
        viewModelScope.launch {
            searchInteractor.addTrackToHistory(track)
        }
    }

    fun restoreState(state: SearchState) {
        _searchState.value = state
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchInteractor.clearSearchHistory()
            _searchState.value = SearchState.EmptyHistory
            isHistoryLoaded = false
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchState.value = SearchState.Empty
        searchJob = viewModelScope.launch {
            val history = searchInteractor.getSearchHistory().first()
            if (history.isEmpty()) {
                _searchState.value = SearchState.EmptyHistory
            } else {
                _searchState.value = SearchState.History(trackUiMapper.mapListToUi(history))
            }
            isHistoryLoaded = true
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}