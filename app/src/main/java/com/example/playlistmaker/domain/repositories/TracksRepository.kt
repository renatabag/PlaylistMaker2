package com.example.playlistmaker.domain.repositories

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun searchTracks(query: String): Flow<List<Track>>
}