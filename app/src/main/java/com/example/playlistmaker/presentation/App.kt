package com.example.playlistmaker.presentation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.creator.Creator

class App : Application() {

    var darkTheme = false

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)
        darkTheme = sharedPreferences.getBoolean("dark_theme", false)
        switchTheme(darkTheme)
        Creator.init(this)
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
        val sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("dark_theme", darkThemeEnabled).apply()
    }


}