package com.example.playlistmaker.data.network

import com.example.playlistmaker.creator.Creator.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
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