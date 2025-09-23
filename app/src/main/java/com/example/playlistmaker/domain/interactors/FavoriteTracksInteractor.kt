package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksInteractor {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
    fun getAllFavorites(): Flow<List<Track>>
    suspend fun isFavorite(trackId: Int): Boolean
}