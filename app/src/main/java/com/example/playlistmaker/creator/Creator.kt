package com.example.playlistmaker.creator

import SearchHistoryRepositoryImpl
import android.content.Context
import android.media.MediaPlayer
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.data.network.RetrofitClient
import com.example.playlistmaker.data.repositories.SettingsRepositoryImpl
import com.example.playlistmaker.data.repositories.TracksRepositoryImpl
import com.example.playlistmaker.data.storage.SharedPrefsStorage
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import com.example.playlistmaker.domain.interactors.impl.PlayerInteractorImpl
import com.example.playlistmaker.domain.interactors.impl.SearchInteractorImpl
import com.example.playlistmaker.domain.interactors.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.repositories.PlayerRepository
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.SettingsRepository
import com.example.playlistmaker.domain.repositories.TracksRepository
import com.example.playlistmaker.domain.repositories.impl.PlayerRepositoryImpl
import com.example.playlistmaker.presentation.viewmodels.SearchViewModel
import com.example.playlistmaker.presentation.viewmodels.SettingsViewModel

object Creator {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }
    internal const val BASE_URL = "https://itunes.apple.com/"

    fun provideSearchViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val api = provideItunesApi()
                val repository = provideTracksRepository(api)
                val searchInteractor = provideSearchInteractor(repository)
                return SearchViewModel(searchInteractor) as T
            }
        }
    }

    private fun provideItunesApi(): ItunesApi = RetrofitClient.itunesApi

    private fun provideTracksRepository(api: ItunesApi): TracksRepository {
        return TracksRepositoryImpl(api, TrackMapper())
    }

    private fun provideSearchInteractor(repository: TracksRepository): SearchInteractor {
        return SearchInteractorImpl(repository, provideSearchHistoryRepository())
    }

    private fun provideSearchHistoryRepository(): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(
            SharedPrefsStorage(requireContext()),
            TrackMapper()
        )
    }
    private fun requireContext(): Context {
        return context ?: throw IllegalStateException("Context not initialized. Call Creator.init() first.")
    }
    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(providePlayerRepository())
    }

    fun providePlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl(MediaPlayer())
    }
    fun provideSettingsInteractor(): SettingsInteractor {
        return SettingsInteractorImpl(provideSettingsRepository())
    }

    private fun provideSettingsRepository(): SettingsRepository {
        return SettingsRepositoryImpl(SharedPrefsStorage(requireContext()))
    }
    fun provideSettingsViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(provideSettingsInteractor()) as T
            }
        }
    }
}