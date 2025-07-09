package com.example.playlistmaker.domain.interactors.impl

import com.example.playlistmaker.domain.interactors.SettingsInteractor
import com.example.playlistmaker.domain.repositories.SettingsRepository

class SettingsInteractorImpl(
    private val settingsRepository: SettingsRepository
) : SettingsInteractor {

    override suspend fun getThemeSettings(): Boolean {
        return settingsRepository.getThemeSettings()
    }

    override suspend fun updateThemeSettings(isDarkTheme: Boolean) {
        settingsRepository.updateThemeSettings(isDarkTheme)
    }
}