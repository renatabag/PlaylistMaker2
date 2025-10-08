package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {
    suspend fun createPlaylist(name: String, description: String? = null, coverImagePath: String? = null): Long
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    fun searchPlaylists(query: String): Flow<List<PlaylistEntity>>
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun getPlaylistCount(): Int
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun savePlaylistTrack(track: PlaylistTrackEntity)
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean
}