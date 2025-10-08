package com.example.playlistmaker.domain.interactors.impl

import android.util.Log
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.repositories.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val playlistRepository: PlaylistRepository
) : PlaylistInteractor {

    override suspend fun createPlaylist(name: String, description: String?, coverImagePath: String?): Long {
        val playlist = PlaylistEntity(
            name = name,
            description = description,
            coverImagePath = coverImagePath
        )
        return playlistRepository.createPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: PlaylistEntity): Boolean {
        return try {
            playlistRepository.updatePlaylist(playlist)
            true
        } catch (e: Exception) {
            Log.e("PlaylistInteractor", "Error updating playlist", e)
            false
        }
    }

    override suspend fun deletePlaylist(playlist: PlaylistEntity) {
        playlistRepository.deletePlaylistById(playlist.id)
    }

    override suspend fun getPlaylistById(playlistId: Long): PlaylistEntity? {
        return playlistRepository.getPlaylistById(playlistId)
    }

    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> {
        return playlistRepository.getAllPlaylists()
    }

    override fun searchPlaylists(query: String): Flow<List<PlaylistEntity>> {
        return playlistRepository.searchPlaylists(query)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        playlistRepository.addTrackToPlaylist(playlistId, trackId)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun getPlaylistCount(): Int {
        return playlistRepository.getPlaylistCount()
    }

    override suspend fun savePlaylistTrack(track: PlaylistTrackEntity) {
        playlistRepository.savePlaylistTrack(track)
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistRepository.isTrackInPlaylist(playlistId, trackId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistRepository.deletePlaylistById(playlistId)
    }
    override suspend fun getPlaylist(playlistId: Long): Playlist? {
        val entity = playlistRepository.getPlaylistById(playlistId)
        return entity?.let {
            Playlist(
                id = it.id,
                name = it.name,
                description = it.description,
                coverImagePath = it.coverImagePath,
                trackCount = it.trackCount,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
    }
}