package com.example.playlistmaker.creator

import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.data.network.RetrofitClient

object NetworkModule {
    fun provideItunesApi(): ItunesApi {
        return RetrofitClient.createRetrofit().create(ItunesApi::class.java)
    }
}