package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.models.PlaylistWithTracks

interface PlaylistTracksInteractor {
    suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTracks
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
}