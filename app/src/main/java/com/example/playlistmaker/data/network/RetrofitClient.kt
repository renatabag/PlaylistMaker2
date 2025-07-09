package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.TrackResponseDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitClient {
    private const val BASE_URL = "https://itunes.apple.com/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val itunesApi: ItunesApi by lazy {
        retrofit.create(ItunesApi::class.java)
    }

}
