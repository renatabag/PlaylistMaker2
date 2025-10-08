package com.example.playlistmaker.data.db.playlist

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    // ИСПРАВЬТЕ этот метод - он должен принимать PlaylistEntity, а не Long
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity) // Было: suspend fun deletePlaylist(playlist: Long)

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%'")
    fun searchPlaylists(query: String): Flow<List<PlaylistEntity>>

    @Query("UPDATE playlists SET trackIdsJson = :trackIdsJson, trackCount = :trackCount, updatedAt = :updatedAt WHERE id = :playlistId")
    suspend fun updatePlaylistTracks(playlistId: Long, trackIdsJson: String, trackCount: Int, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)
}