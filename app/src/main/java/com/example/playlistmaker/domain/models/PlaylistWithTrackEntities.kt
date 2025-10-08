package com.example.playlistmaker.domain.models

import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity

// Временный класс для репозитория
data class PlaylistWithTrackEntities(
    val playlist: PlaylistEntity,
    val tracks: List<PlaylistTrackEntity>
)