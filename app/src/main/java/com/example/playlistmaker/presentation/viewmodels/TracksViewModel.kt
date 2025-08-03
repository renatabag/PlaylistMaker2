package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.presentation.ui.states.FavouritesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TracksViewModel : ViewModel() {
    private val _tracksState = MutableStateFlow<FavouritesState>(FavouritesState.Empty)
    val tracksState: StateFlow<FavouritesState> = _tracksState

    init {
        checkTracks()
    }

    fun checkTracks() {
        _tracksState.value = FavouritesState.Empty
    }


}