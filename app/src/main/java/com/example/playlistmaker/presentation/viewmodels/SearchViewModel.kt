package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.presentation.ui.states.SearchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Empty)
    val searchState: StateFlow<SearchState> = _searchState

    fun performSearch(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            when (val result = searchInteractor.searchTracks(query)) {
                is com.example.playlistmaker.domain.models.SearchState.Content -> {
                    _searchState.value = SearchState.Content(
                        result.tracks.map { ParcelableTrack(it) }
                    )
                }
                com.example.playlistmaker.domain.models.SearchState.Empty -> {
                    _searchState.value = SearchState.Empty
                }
                is com.example.playlistmaker.domain.models.SearchState.Error -> {
                    _searchState.value = SearchState.Error(result.message)
                }
                is com.example.playlistmaker.domain.models.SearchState.History -> {
                    _searchState.value = SearchState.History(
                        result.tracks.map { ParcelableTrack(it) }
                    )
                }
            }
        }
    }

    fun getHistory() {
        viewModelScope.launch {
            val history = searchInteractor.getSearchHistory()
            _searchState.value = if (history.isNotEmpty()) {
                SearchState.History(history.map { ParcelableTrack(it) })
            } else {
                SearchState.Empty
            }
        }
    }

    fun addToHistory(track: com.example.playlistmaker.domain.models.Track) {
        viewModelScope.launch {
            searchInteractor.addTrackToHistory(track)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchInteractor.clearSearchHistory()
            getHistory()
        }
    }
}
