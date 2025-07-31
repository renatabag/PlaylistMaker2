package com.example.playlistmaker.domain.interactors


interface SettingsInteractor {
    suspend fun getThemeSettings(): Boolean
    suspend fun updateThemeSettings(isDarkTheme: Boolean)
}