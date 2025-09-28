package com.example.playlistmaker.DI

import android.media.MediaPlayer
import com.example.playlistmaker.data.NetworkMonitor
import com.example.playlistmaker.data.SearchHistory
import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.RetrofitClient
import com.example.playlistmaker.data.repositories.PlaylistRepositoryImpl
import com.example.playlistmaker.data.storage.SharedPrefsStorage
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.interactors.impl.PlaylistInteractorImpl
import com.example.playlistmaker.domain.repositories.PlaylistRepository
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    // Network
    single { RetrofitClient.itunesApi }

    // Storage
    single { SharedPrefsStorage(androidContext()) }

    // Mappers
    single { TrackMapper() }

    // Network monitor
    single { NetworkMonitor(androidContext()) }

    // Search history
    single { SearchHistory(androidContext(), get()) }

    // Gson
    factory { Gson() }

    // MediaPlayer
    factory { MediaPlayer() }

    // Database
    single {
        AppDatabase.getDatabase(androidContext())
    }
    single {
        AppDatabase.getDatabase(androidContext())
    }

    // DAOs
    single { get<AppDatabase>().favoriteTracksDao() }
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().playlistTrackDao() }

    // Repositories
    single<PlaylistRepository> {
        PlaylistRepositoryImpl(get(), get())
    }

    // Interactors
    single<PlaylistInteractor> {
        PlaylistInteractorImpl(get())
    }
}