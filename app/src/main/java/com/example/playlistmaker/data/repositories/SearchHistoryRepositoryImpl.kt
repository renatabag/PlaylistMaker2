package com.example.playlistmaker.data.repositories

import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.storage.SharedPrefsStorage
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchHistoryRepositoryImpl(
    private val sharedPrefsStorage: SharedPrefsStorage,
    private val mapper: TrackMapper
) : SearchHistoryRepository {

    override fun getHistory(): Flow<List<Track>> = flow {
        val history = sharedPrefsStorage.getSearchHistory().map { mapper.mapToDomain(it) }
        emit(history.distinctBy { it.trackId })
    }

    override suspend fun addTrack(track: Track) {
        val currentHistory = sharedPrefsStorage.getSearchHistory().toMutableList()
        val trackDto = mapper.mapToDto(track)

        currentHistory.removeAll { it.trackId == track.trackId }
        currentHistory.add(0, trackDto)

        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.subList(MAX_HISTORY_SIZE, currentHistory.size).clear()
        }

        sharedPrefsStorage.saveSearchHistory(currentHistory)
    }

    override suspend fun clearHistory() {
        sharedPrefsStorage.clearSearchHistory()
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }
}