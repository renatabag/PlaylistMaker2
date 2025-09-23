package com.example.playlistmaker.data.db

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.ui.states.TrackUi

fun Track.toFavoriteEntity(): FavoriteTrackEntity {
    return FavoriteTrackEntity(
        trackId = this.trackId,
        trackName = this.trackName,
        artistName = this.artistName,
        trackTimeMillis = this.trackTimeMillis,
        artworkUrl = this.artworkUrl,
        collectionName = this.collectionName,
        releaseDate = this.releaseDate,
        genre = this.genre,
        country = this.country,
        previewUrl = this.previewUrl
    )
}

fun FavoriteTrackEntity.toTrack(): Track {
    return Track(
        trackId = this.trackId,
        trackName = this.trackName,
        artistName = this.artistName,
        trackTimeMillis = this.trackTimeMillis,
        artworkUrl = this.artworkUrl,
        collectionName = this.collectionName,
        releaseDate = this.releaseDate,
        genre = this.genre,
        country = this.country,
        previewUrl = this.previewUrl
    )
}

fun FavoriteTrackEntity.toTrackUi(): TrackUi {
    return TrackUi(
        trackId = this.trackId,
        trackName = this.trackName,
        artistName = this.artistName,
        trackTimeMillis = this.trackTimeMillis,
        artworkUrl = this.artworkUrl,
        collectionName = this.collectionName,
        releaseDate = this.releaseDate,
        genre = this.genre,
        country = this.country,
        previewUrl = this.previewUrl
    )
}