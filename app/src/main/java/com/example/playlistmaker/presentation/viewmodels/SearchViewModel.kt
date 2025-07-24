package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.ui.states.SearchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Loading)
    val searchState: StateFlow<SearchState> = _searchState

    fun performSearch(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val tracks = searchInteractor.searchTracks(query)
                _searchState.value = if (tracks.isNotEmpty()) {
                    SearchState.Content(tracks.map { ParcelableTrack(it) })
                } else {
                    SearchState.Empty
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(
                    "Ошибка",
                    e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    fun getHistory() {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val history = searchInteractor.getSearchHistory()
                _searchState.value = if (history.isNotEmpty()) {
                    SearchState.History(history.map(::ParcelableTrack))
                } else {
                    SearchState.Initial // Возвращаем начальное состояние при отсутствии истории
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(
                    "Ошибка истории",
                    e.message ?: "Не удалось загрузить историю"
                )
            }
        }
    }

    fun addToHistory(track: Track) {
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