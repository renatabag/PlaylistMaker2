package com.example.playlistmaker.presentation.ui.states

import com.example.playlistmaker.domain.models.Track

sealed class SearchState {
    object Loading : SearchState()
    data class Content(val tracks: List<TrackUi>) : SearchState()
    object Empty : SearchState()
    data class EmptyError(val message: String) : SearchState()
    data class Error(val message: String) : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
    object EmptyHistory : SearchState()
}