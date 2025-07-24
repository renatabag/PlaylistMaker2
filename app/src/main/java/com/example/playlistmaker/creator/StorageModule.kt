package com.example.playlistmaker.creator

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.data.SearchHistory
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.storage.SharedPrefsStorage
import com.google.gson.Gson

object StorageModule {

    fun provideSharedPreferences(appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences(
            SharedPrefsStorage.SHARED_PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun provideSharedPrefsStorage(
        sharedPreferences: SharedPreferences,
        gson: Gson
    ): SharedPrefsStorage {
        return SharedPrefsStorage(sharedPreferences, gson)
    }

    fun provideSearchHistory(
        sharedPrefsStorage: SharedPrefsStorage,
        trackMapper: TrackMapper
    ): SearchHistory {
        return SearchHistory(sharedPrefsStorage, trackMapper)
    }
}