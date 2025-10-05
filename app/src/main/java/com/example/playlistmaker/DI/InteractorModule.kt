package com.example.playlistmaker.DI

import com.example.playlistmaker.domain.SearchTracksUseCase
import com.example.playlistmaker.domain.interactors.FavoriteTracksInteractor
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import com.example.playlistmaker.domain.interactors.impl.FavoriteTracksInteractorImpl
import com.example.playlistmaker.domain.interactors.impl.PlayerInteractorImpl
import com.example.playlistmaker.domain.interactors.impl.PlaylistInteractorImpl
import com.example.playlistmaker.domain.interactors.impl.SearchInteractorImpl
import com.example.playlistmaker.domain.interactors.impl.SettingsInteractorImpl
import org.koin.dsl.module

val interactorModule = module {
    factory<PlayerInteractor> { PlayerInteractorImpl(get()) }
    factory<SearchInteractor> { SearchInteractorImpl(get(), get()) }
    factory<SettingsInteractor> { SettingsInteractorImpl(get()) }
    factory<FavoriteTracksInteractor> { FavoriteTracksInteractorImpl(get()) }
    factory<PlaylistInteractor> { PlaylistInteractorImpl(get()) }
    factory<FavoriteTracksInteractor> { FavoriteTracksInteractorImpl(get()) }
}