package com.example.playlistmaker.presentation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.data.network.RetrofitClient
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.data.repositories.TracksRepositoryImpl
import com.example.playlistmaker.data.storage.SearchHistoryRepositoryImpl
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.interactors.impl.SearchInteractorImpl
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository

class App : Application() {

    var darkTheme = false
    lateinit var searchInteractor: SearchInteractor

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)
        darkTheme = sharedPreferences.getBoolean("dark_theme", false)
        switchTheme(darkTheme)
        initDependencies()
    }

    private fun initDependencies() {
        val retrofit = RetrofitClient.createRetrofit()
        val iTunesApi = retrofit.create(ItunesApi::class.java)

        val networkClient = RetrofitNetworkClient(iTunesApi)
        val trackMapper = TrackMapper
        val tracksRepository: TracksRepository = TracksRepositoryImpl(networkClient, trackMapper)
        val searchHistoryRepository: SearchHistoryRepository = SearchHistoryRepositoryImpl(this)

        searchInteractor = SearchInteractorImpl(tracksRepository, searchHistoryRepository)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putBoolean("dark_theme", darkThemeEnabled)
            .apply()
    }
}