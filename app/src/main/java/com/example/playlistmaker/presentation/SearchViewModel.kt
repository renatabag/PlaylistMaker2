package com.example.playlistmaker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.data.NetworkMonitor
import com.example.playlistmaker.domain.SearchHistoryRepository
import com.example.playlistmaker.domain.SearchState
import com.example.playlistmaker.domain.SearchTracksUseCase
import com.example.playlistmaker.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val networkMonitor: NetworkMonitor,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Empty)
    val searchState: StateFlow<SearchState> = _searchState

    private var currentQuery = ""

    fun search(query: String) {
        currentQuery = query
        viewModelScope.launch {
            if (networkMonitor.isConnected()) {
                _searchState.value = SearchState.Loading
                _searchState.value = searchTracksUseCase.execute(query)
            } else {
                _searchState.value = SearchState.Error(
                    "Нет подключения",
                    "Проверьте интернет соединение"
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearHistory()
            _searchState.value = SearchState.Empty
        }
    }

    fun addToHistory(track: Track) {
        viewModelScope.launch {
            searchHistoryRepository.addTrack(track)
        }
    }
}