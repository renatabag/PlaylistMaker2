package com.example.playlistmaker.data.mappers

import com.example.playlistmaker.data.dto.TrackDTO
import com.example.playlistmaker.domain.models.Track

class TrackMapper {

    fun mapToDomain(dto: TrackDTO): Track {
        return Track(
            trackId = dto.trackId,
            trackName = dto.trackName ?: "",
            artistName = dto.artistName ?: "",
            trackTimeMillis = dto.trackTimeMillis ?: 0L,
            artworkUrl = dto.artworkUrl100 ?: "",
            collectionName = dto.collectionName,
            releaseDate = dto.releaseDate,
            genre = dto.primaryGenreName,
            country = dto.country,
            previewUrl = dto.previewUrl
        )
    }

    fun mapToDto(track: Track): TrackDTO {
        return TrackDTO(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTimeMillis,
            artworkUrl100 = track.artworkUrl,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.genre,
            country = track.country,
            previewUrl = track.previewUrl
        )
    }
}