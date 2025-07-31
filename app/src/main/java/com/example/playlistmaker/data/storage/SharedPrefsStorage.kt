package com.example.playlistmaker.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.data.dto.TrackDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsStorage(context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        SHARED_PREFS_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun getSearchHistory(): List<TrackDTO> {
        val json = sharedPrefs.getString(SEARCH_HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<TrackDTO>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun saveSearchHistory(tracks: List<TrackDTO>) {
        val json = gson.toJson(tracks)
        sharedPrefs.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }

    fun clearSearchHistory() {
        sharedPrefs.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    fun getThemeSettings(): Boolean {
        return sharedPrefs.getBoolean(THEME_KEY, false)
    }

    fun saveThemeSettings(isDarkTheme: Boolean) {
        sharedPrefs.edit().putBoolean(THEME_KEY, isDarkTheme).apply()
    }

    companion object {
        private const val SHARED_PREFS_NAME = "playlist_maker_preferences"
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val THEME_KEY = "theme_setting"
    }

}