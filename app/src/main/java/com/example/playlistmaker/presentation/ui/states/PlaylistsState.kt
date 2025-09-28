package com.example.playlistmaker.presentation.ui.states

import com.example.playlistmaker.data.db.playlist.PlaylistEntity

sealed class PlaylistsState {
    object Loading : PlaylistsState()
    object Empty : PlaylistsState()
    data class Content(val playlists: List<PlaylistEntity>) : PlaylistsState()
    data class Error(val message: String) : PlaylistsState()
}