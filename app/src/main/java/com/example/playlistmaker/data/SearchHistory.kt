package com.example.playlistmaker.data

import com.example.playlistmaker.data.dto.TrackDTO
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.storage.SharedPrefsStorage
import com.example.playlistmaker.domain.models.Track

class SearchHistory(
    private val sharedPrefsStorage: SharedPrefsStorage,
    private val trackMapper: TrackMapper,
    private val maxHistorySize: Int = 10
) {
    private val historyKey = "search_history"

    fun addTrack(track: Track) {
        val historyDto = getHistoryAsDto().toMutableList()
        historyDto.removeAll { it.trackId == track.trackId }
        historyDto.add(0, trackMapper.mapToDto(track))

        if (historyDto.size > maxHistorySize) {
            historyDto.subList(maxHistorySize, historyDto.size).clear()
        }
        saveHistory(historyDto)
    }

    fun getHistory(): List<Track> {
        return getHistoryAsDto().map { trackMapper.mapToDomain(it) }
    }

    private fun getHistoryAsDto(): List<TrackDTO> {
        return sharedPrefsStorage.getSearchHistory()
    }

    fun clearHistory() {
        sharedPrefsStorage.clearSearchHistory()
    }

    private fun saveHistory(history: List<TrackDTO>) {
        sharedPrefsStorage.saveSearchHistory(history)
    }
}