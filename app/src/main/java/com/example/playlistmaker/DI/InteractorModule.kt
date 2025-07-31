package com.example.playlistmaker.DI

import com.example.playlistmaker.domain.SearchTracksUseCase
import com.example.playlistmaker.domain.TrackUtils
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import com.example.playlistmaker.domain.interactors.impl.PlayerInteractorImpl
import com.example.playlistmaker.domain.interactors.impl.SearchInteractorImpl
import com.example.playlistmaker.domain.interactors.impl.SettingsInteractorImpl
import org.koin.dsl.module

val interactorModule = module {
    single<PlayerInteractor> { PlayerInteractorImpl(get()) }
    single<SearchInteractor>{ SearchInteractorImpl(get(),get()) }
    single<SettingsInteractor> { SettingsInteractorImpl(get()) }
    single { TrackUtils }

    factory {
        SearchTracksUseCase(
            tracksRepository = get(),
            searchHistoryRepository = get(),
            trackUiMapper = get()
        )
    }
}