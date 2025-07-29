package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _isDarkTheme = MutableLiveData<Boolean>()
    val isDarkTheme: LiveData<Boolean> = _isDarkTheme

    init {
        loadThemeSettings()
    }

    private fun loadThemeSettings() {
        viewModelScope.launch {
            _isDarkTheme.postValue(settingsInteractor.getThemeSettings())
        }
    }

    fun switchTheme(isDarkTheme: Boolean) {
        viewModelScope.launch {
            settingsInteractor.updateThemeSettings(isDarkTheme)
            _isDarkTheme.postValue(isDarkTheme)
        }
    }
}