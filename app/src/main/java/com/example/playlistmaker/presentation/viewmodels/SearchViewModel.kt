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
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Empty)
    val searchState: StateFlow<SearchState> = _searchState

    private var searchJob: Job? = null

    fun searchTracks(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            searchJob = viewModelScope.launch {
                val history = searchInteractor.getSearchHistory()
                if (history.isEmpty()) {
                    _searchState.value = SearchState.EmptyHistory // Используем новое состояние
                } else {
                    _searchState.value = SearchState.History(history)
                }
            }
            return
        }
        _searchState.value = SearchState.Loading
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)

            try {
                when (val result = searchInteractor.searchTracks(query)) {
                    is SearchInteractor.SearchResult.Content -> {
                        _searchState.value = SearchState.Content(TrackUiMapper.mapListToUi(result.tracks))
                    }
                    SearchInteractor.SearchResult.Empty -> {
                        _searchState.value = SearchState.Empty // Используем новое состояние
                    }
                    is SearchInteractor.SearchResult.Error -> {
                        _searchState.value = SearchState.Error(result.message)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addTrackToHistory(track: Track) {
        viewModelScope.launch {
            searchInteractor.addTrackToHistory(track)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchInteractor.clearSearchHistory()
            _searchState.value = SearchState.EmptyHistory
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}