package com.example.playlistmaker.data.repositories

import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.NetworkClient
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.TracksRepository

class TracksRepositoryImpl(
    private val networkClient: NetworkClient,
    private val trackMapper: TrackMapper
) : TracksRepository {

    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            val trackDTOs = networkClient.searchTracks(query)
            trackMapper.mapListToDomain(trackDTOs)
        } catch (e: Exception) {
            emptyList()
        }
    }
}