package com.example.playlistmaker.data.repositories

import android.util.Log
import com.example.playlistmaker.data.db.favouriteTracks.FavoriteTracksDao
import com.example.playlistmaker.data.db.favouriteTracks.toFavoriteEntity
import com.example.playlistmaker.data.db.favouriteTracks.toTrack
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val favoriteTracksDao: FavoriteTracksDao
) : FavoriteTracksRepository {

    companion object {
        private const val TAG = "FavoriteTracksRepo"
    }

    override fun getAllFavorites(): Flow<List<Track>> {
        return favoriteTracksDao.getAllFavorites()
            .map { favorites -> favorites.map { it.toTrack() } }
    }

    override suspend fun addToFavorites(track: Track) {
        try {
            Log.d(TAG, "Adding track to favorites: ${track.trackName}")
            favoriteTracksDao.insert(track.toFavoriteEntity())
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to favorites: ${e.message}", e)
        }
    }

    override suspend fun removeFromFavorites(track: Track) {
        try {
            Log.d(TAG, "Removing track from favorites: ${track.trackName}")
            favoriteTracksDao.delete(track.toFavoriteEntity())
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from favorites: ${e.message}", e)
        }
    }

    override suspend fun isFavorite(trackId: Int): Boolean {
        return favoriteTracksDao.isFavorite(trackId)
    }

    override suspend fun getFavoriteIds(): List<Int> {
        return favoriteTracksDao.getAllFavoriteIds()
    }


}