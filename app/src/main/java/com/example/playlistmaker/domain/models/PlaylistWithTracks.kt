package com.example.playlistmaker.domain.models

import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.presentation.ui.states.TrackUi

data class PlaylistWithTracks(
    val playlist: PlaylistEntity,
    val tracks: List<TrackUi>
)