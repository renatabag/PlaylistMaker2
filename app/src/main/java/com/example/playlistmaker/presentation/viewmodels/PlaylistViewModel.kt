package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.PlaylistTracksInteractor
import com.example.playlistmaker.presentation.ui.states.PlaylistState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistTracksInteractor: PlaylistTracksInteractor
) : ViewModel() {

    private val _playlistState = MutableStateFlow<PlaylistState>(PlaylistState.Loading)
    val playlistState: StateFlow<PlaylistState> = _playlistState.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                _playlistState.value = PlaylistState.Loading
                val playlistWithTracks = playlistTracksInteractor.getPlaylistWithTracks(playlistId)

                // Проверяем, есть ли треки в плейлисте
                if (playlistWithTracks.tracks.isEmpty()) {
                    _playlistState.value = PlaylistState.Empty
                } else {
                    _playlistState.value = PlaylistState.Content(
                        playlist = playlistWithTracks.playlist,
                        tracks = playlistWithTracks.tracks
                    )
                }
            } catch (e: Exception) {
                _playlistState.value = PlaylistState.Error(e.message ?: "Unknown error")
            }
        }
    }
}