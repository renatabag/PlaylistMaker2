package com.example.playlistmaker.domain.repositories

import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity
import com.example.playlistmaker.domain.models.PlaylistWithTrackEntities
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: PlaylistEntity): Long
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    fun searchPlaylists(query: String): Flow<List<PlaylistEntity>>
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun getPlaylistCount(): Int
    suspend fun deletePlaylistById(playlistId: Long)
    suspend fun savePlaylistTrack(track: PlaylistTrackEntity)
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean
    suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTrackEntities

}