package com.example.playlistmaker.DI

import com.example.playlistmaker.presentation.viewmodels.FavoritesViewModel
import com.example.playlistmaker.presentation.viewmodels.PlayListsViewModel
import com.example.playlistmaker.presentation.viewmodels.PlayerViewModel
import com.example.playlistmaker.presentation.viewmodels.SearchViewModel
import com.example.playlistmaker.presentation.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { PlayerViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { PlayListsViewModel(get()) }
}