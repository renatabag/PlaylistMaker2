package com.example.playlistmaker.presentation.ui.states

import com.example.playlistmaker.data.db.playlist.PlaylistEntity

sealed class PlaylistState {
    object Loading : PlaylistState()
    object Empty : PlaylistState()
    data class Content(
        val playlist: PlaylistEntity,
        val tracks: List<TrackUi>
    ) : PlaylistState()
    data class Error(val message: String) : PlaylistState()
}