package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.models.Track
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
        searchJob = viewModelScope.launch {
            _searchState.value = SearchState.Loading
            delay(SEARCH_DEBOUNCE_DELAY)

            try {
                val result = searchInteractor.searchTracks(query)
                _searchState.value = when (result) {
                    is com.example.playlistmaker.domain.models.SearchState.Content ->
                        SearchState.Content(result.tracks)
                    is com.example.playlistmaker.domain.models.SearchState.Empty ->
                        SearchState.Empty
                    is com.example.playlistmaker.domain.models.SearchState.Error ->
                        SearchState.Error(result.message)
                    is com.example.playlistmaker.domain.models.SearchState.History ->
                        SearchState.History(result.tracks)
                    else -> SearchState.Empty
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
            _searchState.value = SearchState.Empty
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}