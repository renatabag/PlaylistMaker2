package com.example.playlistmaker.data.mappers

import com.example.playlistmaker.data.dto.TrackDTO
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.viewmodels.ParcelableTrack

object TrackMapper {

    fun mapToDomain(dto: TrackDTO): Track {
        return Track(
            trackId = dto.trackId.toInt(),
            trackName = dto.trackName ?: "",
            artistName = dto.artistName ?: "",
            trackTimeMillis = dto.trackTimeMillis ?: 0L,
            artworkUrl = dto.artworkUrl100?.replace("100x100bb", "512x512bb") ?: "",
            collectionName = dto.collectionName,
            releaseDate = dto.releaseDate,
            genre = dto.primaryGenreName,
            country = dto.country,
            previewUrl = dto.previewUrl
        )
    }
    fun mapToDto(track: Track): TrackDTO {
        return TrackDTO(
            trackId = track.trackId.toLong(), // Конвертируем обратно в Long
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

    fun mapListToDomain(trackDTOs: List<TrackDTO>): List<Track> {
        return trackDTOs.map { mapToDomain(it) }
    }

    fun mapToParcelable(track: Track): ParcelableTrack = ParcelableTrack(track)
    fun mapListToParcelable(tracks: List<Track>): List<ParcelableTrack> = tracks.map(::mapToParcelable)
}