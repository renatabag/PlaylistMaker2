package com.example.playlistmaker.DI

import com.example.playlistmaker.data.repositories.PlayerRepositoryImpl
import com.example.playlistmaker.data.repositories.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repositories.SettingsRepositoryImpl
import com.example.playlistmaker.data.repositories.TracksRepositoryImpl
import com.example.playlistmaker.domain.repositories.PlayerRepository
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.SettingsRepository
import com.example.playlistmaker.domain.repositories.TracksRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<PlayerRepository> { PlayerRepositoryImpl(get()) }
    single<SearchHistoryRepository>{ SearchHistoryRepositoryImpl(get(),get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<TracksRepository>{ TracksRepositoryImpl(get(),get()) }
}