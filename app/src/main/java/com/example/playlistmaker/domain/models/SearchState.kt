package com.example.playlistmaker.domain.models

sealed class SearchState {
    data class Content(val tracks: List<Track>) : SearchState()
    object Empty : SearchState()
    data class Error(val message: String, val string: String) : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
}