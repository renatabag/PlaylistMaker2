package com.example.playlistmaker.DI

import android.media.MediaPlayer
import com.example.playlistmaker.data.NetworkMonitor
import com.example.playlistmaker.data.SearchHistory
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.RetrofitClient
import com.example.playlistmaker.data.storage.SharedPrefsStorage
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single{ RetrofitClient.itunesApi }

    single{ SharedPrefsStorage(androidContext()) }

    single{ TrackMapper() }

    single { NetworkMonitor(androidContext()) }
    single { SearchHistory(androidContext(), get()) }
    factory { Gson() }

    factory { MediaPlayer() }
}