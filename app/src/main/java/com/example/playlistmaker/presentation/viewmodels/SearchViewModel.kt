package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.mappers.TrackUiMapper
import com.example.playlistmaker.presentation.ui.states.SearchState
import com.example.playlistmaker.presentation.ui.states.SearchState.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val trackUiMapper: TrackUiMapper
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.EmptyHistory)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        setupSearchFlow()
        loadInitialHistory()
    }

    private fun setupSearchFlow() {
        searchQuery
            .debounce(500) // Дебаунс 500ms
            .distinctUntilChanged()
            .filter { it.isNotBlank() || it.isEmpty() }
            .onEach { query ->
                performSearch(query)
            }
            .launchIn(viewModelScope)
    }

    private fun loadInitialHistory() {
        searchQuery.value = ""
    }

    fun searchTracks(query: String) {
        searchQuery.value = query
    }

    private fun performSearch(query: String) {
        searchInteractor.searchTracks(query)
            .onStart {
                if (query.isNotBlank()) {
                    _searchState.value = SearchState.Loading
                }
            }
            .onEach { result ->
                when (result) {
                    is SearchInteractor.SearchResult.Content -> {
                        _searchState.value = Content(trackUiMapper.mapListToUi(result.tracks))
                    }
                    SearchInteractor.SearchResult.Empty -> {
                        _searchState.value = Empty
                    }
                    is SearchInteractor.SearchResult.Error -> {
                        _searchState.value = Error(result.message, result.message)
                    }
                    is SearchInteractor.SearchResult.History -> {
                        _searchState.value = History(trackUiMapper.mapListToUi(result.tracks))
                    }

                    is SearchInteractor.SearchResult.EmptyError -> {
                        _searchState.value = Empty
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun addTrackToHistory(track: Track) {
        viewModelScope.launch {
            searchInteractor.addTrackToHistory(track)
            searchQuery.value = ""
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchInteractor.clearSearchHistory()
            _searchState.value = EmptyHistory
        }
    }

    fun clearSearch() {
        searchQuery.value = ""
    }
}