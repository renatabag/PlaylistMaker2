package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.TrackDTO
import com.example.playlistmaker.data.dto.TrackResponseDto
import retrofit2.Response

class RetrofitNetworkClient(
    private val iTunesApi: ItunesApi
) : NetworkClient {

    override suspend fun searchTracks(query: String): List<TrackDTO> {
        val response: Response<TrackResponseDto> = iTunesApi.search(query)
        if (response.isSuccessful) {
            val body = response.body()
            // Исправлено с tracks на results
            return body?.tracks ?: emptyList()
        } else {
            throw Exception("Network error: ${response.code()} - ${response.message()}")
        }
    }
}