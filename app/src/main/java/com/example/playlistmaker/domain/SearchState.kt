package com.example.playlistmaker.domain

sealed class SearchState {
    object Loading : SearchState()
    data class Content(val tracks: List<Track>) : SearchState()
    object Empty : SearchState()
    data class Error(val title: String, val message: String) : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
}