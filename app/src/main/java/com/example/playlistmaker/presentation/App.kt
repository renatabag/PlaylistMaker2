package com.example.playlistmaker.presentation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.DI.dataModule
import com.example.playlistmaker.DI.interactorModule
import com.example.playlistmaker.DI.repositoryModule
import com.example.playlistmaker.DI.viewModelModule
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {
    private val settingsInteractor: SettingsInteractor by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                dataModule,
                repositoryModule,
                interactorModule,
                viewModelModule
            )
        }

        runBlocking {
            val darkTheme = settingsInteractor.getThemeSettings()
            AppCompatDelegate.setDefaultNightMode(
                if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}