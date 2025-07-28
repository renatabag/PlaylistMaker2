package com.example.playlistmaker.creator

import SearchHistoryRepositoryImpl
import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.data.network.RetrofitClient
import com.example.playlistmaker.data.repositories.TracksRepositoryImpl
import com.example.playlistmaker.data.storage.SharedPrefsStorage
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.interactors.impl.SearchInteractorImpl
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TracksRepository
import com.example.playlistmaker.presentation.viewmodels.SearchViewModel

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
}