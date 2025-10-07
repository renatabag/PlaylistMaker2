package com.example.playlistmaker.domain.interactors.impl

import com.example.playlistmaker.domain.interactors.PlaylistTracksInteractor
import com.example.playlistmaker.domain.models.PlaylistWithTracks
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.PlaylistRepository
import com.example.playlistmaker.presentation.mappers.TrackUiMapper

class PlaylistTracksInteractorImpl(
    private val playlistRepository: PlaylistRepository,
    private val trackUiMapper: TrackUiMapper
) : PlaylistTracksInteractor {

    override suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTracks {
        val playlistWithTracks = playlistRepository.getPlaylistWithTracks(playlistId)
        val trackUiList = playlistWithTracks.tracks.map { trackEntity ->
            // Конвертируем PlaylistTrackEntity в Track, а затем в TrackUi
            val track = Track(
                trackId = trackEntity.trackId.toInt(),
                trackName = trackEntity.trackName,
                artistName = trackEntity.artistName,
                trackTimeMillis = trackEntity.trackTimeMillis,
                artworkUrl = trackEntity.artworkUrl ?: "",
                collectionName = trackEntity.collectionName,
                releaseDate = trackEntity.releaseDate,
                genre = trackEntity.genre,
                country = trackEntity.country,
                previewUrl = trackEntity.previewUrl
            )
            trackUiMapper.mapToUi(track)
        }

        return PlaylistWithTracks(
            playlist = playlistWithTracks.playlist,
            tracks = trackUiList
        )
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        playlistRepository.addTrackToPlaylist(playlistId, trackId)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
    }
}