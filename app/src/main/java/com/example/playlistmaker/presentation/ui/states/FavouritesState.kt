package com.example.playlistmaker.presentation.ui.states


sealed class FavouritesState {
    object Empty : FavouritesState()
    data class Content(val tracks: List<TrackUi>) : FavouritesState()
}