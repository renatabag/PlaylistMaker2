package com.example.playlistmaker.data.repositories

import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class TracksRepositoryImpl(
    private val itunesApi: ItunesApi,
    private val trackMapper: TrackMapper
) : TracksRepository {

    override fun searchTracks(query: String): Flow<List<Track>> = flow {
        try {
            val response: Response<*> = itunesApi.search(query)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody is com.example.playlistmaker.data.dto.TrackResponseDto) {
                    val tracks = responseBody.tracks.map { trackMapper.mapToDomain(it) } ?: emptyList()
                    emit(tracks)
                } else {
                    emit(emptyList())
                }
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}