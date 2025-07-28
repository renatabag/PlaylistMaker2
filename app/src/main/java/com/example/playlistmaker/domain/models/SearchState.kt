// domain/models/SearchState.kt
package com.example.playlistmaker.domain.models

sealed class SearchState {
    object Loading : SearchState()
    data class Content(val tracks: List<Track>) : SearchState()
    object Empty : SearchState()
    data class EmptyError(val message: String) : SearchState()
    data class Error(val message: String, val string: String) : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
}