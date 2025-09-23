// MapperModule.kt
package com.example.playlistmaker.DI

import com.example.playlistmaker.presentation.mappers.TrackUiMapper
import org.koin.dsl.module

val mapperModule = module {single { TrackUiMapper }
    factory { TrackUiMapper}
}