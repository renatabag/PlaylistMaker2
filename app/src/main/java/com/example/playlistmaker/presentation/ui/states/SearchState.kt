package com.example.playlistmaker.presentation.ui.states

sealed class SearchState {
    object Loading : SearchState()
    data class Content(val tracks: List<TrackUi>) : SearchState()
    object Empty : SearchState()
    data class EmptyError(val message: String) : SearchState()
    data class Error(val message: String, val string: String) : SearchState()
    data class History(val tracks: List<TrackUi>) : SearchState()
    object EmptyHistory : SearchState()

}