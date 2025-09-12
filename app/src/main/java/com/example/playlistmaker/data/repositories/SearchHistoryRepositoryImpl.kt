package com.example.playlistmaker.data.repositories

import com.example.playlistmaker.data.storage.SharedPrefsStorage
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.reflect.Type

class SearchHistoryRepositoryImpl(
    private val storage: SharedPrefsStorage,
    private val gson: Gson
) : SearchHistoryRepository {

    private val trackListType: Type = object : TypeToken<List<Track>>() {}.type

    override fun getHistory(): Flow<List<Track>> = flow {
        val historyJson = storage.getSearchHistory()
        val history = parseHistoryFromJson(historyJson)
        emit(history)
    }

    override suspend fun addTrack(track: Track) {
        val currentHistory = parseHistoryFromJson(storage.getSearchHistory())
        val updatedHistory = (listOf(track) + currentHistory.filter { it.trackId != track.trackId })
            .take(10)
        storage.saveSearchHistory(updatedHistory)
    }

    override suspend fun clearHistory() {
        storage.clearSearchHistory()
    }

    private fun parseHistoryFromJson(json: String): List<Track> {
        return if (json.isNotEmpty()) {
            try {
                gson.fromJson(json, trackListType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}