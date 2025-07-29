package com.example.playlistmaker.presentation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import kotlinx.coroutines.runBlocking

class App : Application() {
    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate() {
        super.onCreate()
        Creator.init(this)
        settingsInteractor = Creator.provideSettingsInteractor()

        runBlocking {
            val darkTheme = settingsInteractor.getThemeSettings()
            AppCompatDelegate.setDefaultNightMode(
                if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}