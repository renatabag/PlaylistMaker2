package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.presentation.ui.states.PlaylistsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PlayListsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {
    private val _playlistsState = MutableStateFlow<PlaylistsState>(PlaylistsState.Loading)
    val playlistsState: StateFlow<PlaylistsState> = _playlistsState.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists()
                .onStart {
                    _playlistsState.value = PlaylistsState.Loading
                }
                .collect { playlists ->
                    if (playlists.isEmpty()) {
                        _playlistsState.value = PlaylistsState.Empty
                    } else {
                        _playlistsState.value = PlaylistsState.Content(playlists)
                    }
                }
        }
    }

    fun refreshPlaylists() {
        loadPlaylists()
    }
}