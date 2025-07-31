// presentation/mappers/TrackMapper.kt
package com.example.playlistmaker.presentation.mappers

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.ui.states.TrackUi

object TrackMapper {
    fun mapToUi(track: TrackUi): TrackUi {
        return TrackUi.fromDomain(track)
    }

    fun mapToDomain(trackUi: TrackUi): Track {
        return TrackUi.toDomain(trackUi)
    }

    fun mapListToUi(tracks: List<TrackUi>): List<TrackUi> {
        return tracks.map { mapToUi(it) }
    }
}