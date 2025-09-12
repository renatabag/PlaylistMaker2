package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.TrackResponseDto
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("search?entity=song")
    suspend fun search(@Query("term") text: String): Response<TrackResponseDto>
    @GET("search?entity=song")
    fun searchFlow(@Query("term") text: String): Flow<Response<TrackResponseDto>>
}