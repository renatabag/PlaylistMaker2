package com.example.playlistmaker.domain.interactors.impl

import com.example.playlistmaker.domain.interactors.FavoriteTracksInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow

class FavoriteTracksInteractorImpl(
    private val favoriteTracksRepository: FavoriteTracksRepository
) : FavoriteTracksInteractor {

    override suspend fun addToFavorites(track: Track) {
        favoriteTracksRepository.addToFavorites(track)
    }

    override suspend fun removeFromFavorites(track: Track) {
        favoriteTracksRepository.removeFromFavorites(track)
    }

    override fun getAllFavorites(): Flow<List<Track>> {
        return favoriteTracksRepository.getAllFavorites()
    }

    override suspend fun isFavorite(trackId: Int): Boolean {
        return favoriteTracksRepository.isFavorite(trackId)
    }
}