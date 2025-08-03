package com.example.playlistmaker.DI

import com.example.playlistmaker.presentation.viewmodels.PlayerViewModel
import com.example.playlistmaker.presentation.viewmodels.SearchViewModel
import com.example.playlistmaker.presentation.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel{
        PlayerViewModel(get())
    }
    viewModel{
        SearchViewModel(get())
    }
    viewModel{
        SettingsViewModel(get())
    }
}