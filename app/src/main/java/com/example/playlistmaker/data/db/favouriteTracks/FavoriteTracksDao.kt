package com.example.playlistmaker.data.db.favouriteTracks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTracksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: FavoriteTrackEntity)

    @Delete
    suspend fun delete(track: FavoriteTrackEntity)

    @Query("SELECT * FROM favorite_tracks ORDER BY added_timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteTrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getAllFavoriteIds(): List<Int>

    @Query("SELECT * FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun getFavoriteById(trackId: Int): FavoriteTrackEntity?

    @Query("SELECT COUNT(*) FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun isFavorite(trackId: Int): Boolean
}