package com.example.playlistmaker.presentation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import kotlinx.coroutines.runBlocking

class App : Application() {

    private lateinit var settingsInteractor: SettingsInteractor
    var darkTheme = false
        private set

    override fun onCreate() {
        super.onCreate()
        Creator.init(this)
        settingsInteractor = Creator.provideSettingsInteractor()

        runBlocking {
            darkTheme = settingsInteractor.getThemeSettings()
            switchTheme(darkTheme)
        }
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )

        runBlocking {
            settingsInteractor.updateThemeSettings(darkThemeEnabled)
        }
    }
}