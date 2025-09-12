// UseCaseModule.kt
package com.example.playlistmaker.DI

import com.example.playlistmaker.domain.SearchTracksUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory {
        SearchTracksUseCase(
            tracksRepository = get(),
            searchHistoryRepository = get(),
            trackUiMapper = get()
        )
    }
}