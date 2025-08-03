package com.example.playlistmaker.domain.repositories

interface SettingsRepository {
    suspend fun getThemeSettings(): Boolean
    suspend fun updateThemeSettings(isDarkTheme: Boolean)

}