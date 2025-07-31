package com.example.playlistmaker.presentation.mappers

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.ui.states.TrackUi

object TrackUiMapper {
    fun mapToUi(track: Track): TrackUi {
        return TrackUi(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl = track.artworkUrl,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            genre = track.genre,
            country = track.country,
            previewUrl = track.previewUrl
        )
    }

    fun mapToDomain(trackUi: TrackUi): Track {
        return Track(
            trackId = trackUi.trackId,
            trackName = trackUi.trackName,
            artistName = trackUi.artistName,
            trackTimeMillis = trackUi.trackTimeMillis,
            artworkUrl = trackUi.artworkUrl,
            collectionName = trackUi.collectionName,
            releaseDate = trackUi.releaseDate,
            genre = trackUi.genre,
            country = trackUi.country,
            previewUrl = trackUi.previewUrl
        )
    }

    fun mapListToUi(tracks: List<Track>): List<TrackUi> {
        return tracks.map { mapToUi(it) }
    }

    fun mapListToDomain(tracksUi: List<TrackUi>): List<Track> {
        return tracksUi.map { mapToDomain(it) }
    }
}