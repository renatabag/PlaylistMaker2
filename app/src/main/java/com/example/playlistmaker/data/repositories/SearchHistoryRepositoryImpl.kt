package com.example.playlistmaker.data.storage

import android.content.Context
import com.example.playlistmaker.data.dto.TrackDTO
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    context: Context
) : SearchHistoryRepository {

    private val sharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val mapper = TrackMapper

    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString("history", null)
        return if (json != null) {
            val type = object : TypeToken<List<TrackDTO>>() {}.type
            val historyDto: List<TrackDTO> = gson.fromJson(json, type)
            historyDto.map { mapper.mapToDomain(it) }
        } else {
            emptyList()
        }
    }

    override suspend fun addTrack(track: Track) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.removeAll { it.trackId == track.trackId }
        currentHistory.add(0, track)

        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
        }

        saveHistory(currentHistory)
    }

    override suspend fun clearHistory() {
        sharedPreferences.edit().remove("history").apply()
    }

    private fun saveHistory(history: List<Track>) {
        val historyDto = history.map { mapper.mapToDto(it) }
        val json = gson.toJson(historyDto)
        sharedPreferences.edit().putString("history", json).apply()
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }
}