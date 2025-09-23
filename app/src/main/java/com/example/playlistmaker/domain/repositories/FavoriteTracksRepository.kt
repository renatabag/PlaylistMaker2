package com.example.playlistmaker.domain.repositories

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
    fun getAllFavorites(): Flow<List<Track>>
    suspend fun isFavorite(trackId: Int): Boolean
    suspend fun getFavoriteIds(): List<Int>
}