package com.example.playlistmaker.data.repositories

import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.TracksRepository

class TracksRepositoryImpl(
    private val itunesApi: ItunesApi,
    private val trackMapper: TrackMapper
) : TracksRepository {

    override suspend fun searchTracks(query: String): List<Track> {
        val response = itunesApi.search(query)
        return if (response.isSuccessful) {
            response.body()?.tracks?.map { trackMapper.mapToDomain(it) } ?: emptyList()
        } else {
            emptyList()
        }
    }
}