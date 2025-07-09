package com.example.playlistmaker.data.repositories

import com.example.playlistmaker.data.storage.SharedPrefsStorage
import com.example.playlistmaker.domain.repositories.SettingsRepository

class SettingsRepositoryImpl(
    private val storage: SharedPrefsStorage
) : SettingsRepository {

    override suspend fun getThemeSettings(): Boolean {
        return storage.getThemeSettings()
    }

    override suspend fun updateThemeSettings(isDarkTheme: Boolean) {
        storage.saveThemeSettings(isDarkTheme)
    }
}
