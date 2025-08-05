package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.presentation.ui.states.PlaylistsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayListsViewModel : ViewModel() {
    private val _playlistsState = MutableStateFlow<PlaylistsState>(PlaylistsState.Empty)
    val playlistsState: StateFlow<PlaylistsState> = _playlistsState

    init {
        checkPlaylists()
    }

    fun checkPlaylists() {
        _playlistsState.value = PlaylistsState.Empty
    }


}