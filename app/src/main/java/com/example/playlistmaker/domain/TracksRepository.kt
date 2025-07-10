package com.example.playlistmaker.domain

interface TracksRepository {
    suspend fun searchTracks(query: String): List<Track>
}