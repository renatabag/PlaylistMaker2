package com.example.playlistmaker.DI

import com.example.playlistmaker.utils.Debouncer
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

val utilsModule = module {
    factory { (scope: CoroutineScope) -> Debouncer(scope) }
}