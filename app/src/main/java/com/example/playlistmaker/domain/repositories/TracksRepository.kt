package com.example.playlistmaker.domain.repositories

import com.example.playlistmaker.domain.models.Track

interface TracksRepository {
    suspend fun searchTracks(query: String): List<Track>
}