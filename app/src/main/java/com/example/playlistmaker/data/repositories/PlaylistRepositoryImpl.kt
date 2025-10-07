package com.example.playlistmaker.data.repositories

import com.example.playlistmaker.data.db.playlist.PlaylistDao
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.data.db.playlist.PlaylistTrackDao
import com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity
import com.example.playlistmaker.domain.models.PlaylistWithTrackEntities
import com.example.playlistmaker.domain.models.PlaylistWithTracks
import com.example.playlistmaker.domain.repositories.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao  // Добавьте этот параметр
) : PlaylistRepository {

    // Существующие методы остаются без изменений...
    override suspend fun createPlaylist(playlist: PlaylistEntity): Long {
        return playlistDao.insertPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: PlaylistEntity) {
        playlistDao.updatePlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlist: PlaylistEntity) {
        playlistDao.deletePlaylist(playlist)
    }

    override suspend fun getPlaylistById(playlistId: Long): PlaylistEntity? {
        return playlistDao.getPlaylistById(playlistId)
    }

    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> {
        return playlistDao.getAllPlaylists()
    }

    override fun searchPlaylists(query: String): Flow<List<PlaylistEntity>> {
        return playlistDao.searchPlaylists(query)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val updatedPlaylist = playlist.addTrack(trackId)
        playlistDao.updatePlaylist(updatedPlaylist)
    }

    override suspend fun getPlaylistCount(): Int {
        return playlistDao.getPlaylistCount()
    }

    override suspend fun deletePlaylistById(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }

    // Новые методы
    override suspend fun savePlaylistTrack(track: PlaylistTrackEntity) {
        playlistTrackDao.insertTrack(track)
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        val playlist = playlistDao.getPlaylistById(playlistId)
        return playlist?.containsTrack(trackId) ?: false
    }
    override suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTrackEntities {
        val playlist = playlistDao.getPlaylistById(playlistId)
            ?: throw IllegalArgumentException("Playlist not found")

        val trackIds = playlist.getTrackIds()
        val tracks = if (trackIds.isNotEmpty()) {
            playlistTrackDao.getTracksByIds(trackIds)
        } else {
            emptyList()
        }

        return PlaylistWithTrackEntities(playlist, tracks)
    }


    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        val playlist = playlistDao.getPlaylistById(playlistId)
            ?: throw IllegalArgumentException("Playlist not found")

        val updatedPlaylist = playlist.removeTrack(trackId)
        playlistDao.updatePlaylist(updatedPlaylist)
    }
}