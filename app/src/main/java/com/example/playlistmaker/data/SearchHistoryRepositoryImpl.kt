package com.example.playlistmaker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.data.dto.TrackDTO
import com.example.playlistmaker.domain.SearchHistoryRepository
import com.example.playlistmaker.domain.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val context: Context,
    private val trackMapper: TrackMapper
) : SearchHistoryRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val maxHistorySize = 10

    override fun addTrack(track: Track) {
        val historyDto = getHistoryAsDto().toMutableList()
        historyDto.removeAll { it.trackId == track.trackId }
        historyDto.add(0, trackMapper.mapToDto(track))

        if (historyDto.size > maxHistorySize) {
            historyDto.subList(maxHistorySize, historyDto.size).clear()
        }
        saveHistory(historyDto)
    }

    override fun getHistory(): List<Track> {
        return getHistoryAsDto().map { trackMapper.map(it) }
    }

    private fun getHistoryAsDto(): List<TrackDTO> {
        val json = sharedPreferences.getString("history", null)
        return if (json == null) {
            emptyList()
        } else {
            val type = object : TypeToken<List<TrackDTO>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove("history").apply()
    }

    private fun saveHistory(history: List<TrackDTO>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString("history", json).apply()
    }
}