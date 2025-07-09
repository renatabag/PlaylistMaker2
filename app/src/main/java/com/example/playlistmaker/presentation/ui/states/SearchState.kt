package com.example.playlistmaker.presentation.ui.states

import com.example.playlistmaker.domain.models.Track

sealed class SearchState {
    data class Content(val tracks: List<Track>) : SearchState()
    object Empty : SearchState()
    data class Error(val message: String) : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
    object Loading : SearchState()
}