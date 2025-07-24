package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.TrackDTO

interface NetworkClient {
    suspend fun searchTracks(query: String): List<TrackDTO>
}