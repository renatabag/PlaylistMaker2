package com.example.playlistmaker.presentation.ui.states

import com.example.playlistmaker.presentation.viewmodels.ParcelableTrack

sealed class SearchState {
    data class Content(val tracks: List<ParcelableTrack>) : SearchState()
    object Empty : SearchState()
    data class Error(val message: String) : SearchState()
    data class History(val tracks: List<ParcelableTrack>) : SearchState()
    object Loading : SearchState()
}