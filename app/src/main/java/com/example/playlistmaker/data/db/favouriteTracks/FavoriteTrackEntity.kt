package com.example.playlistmaker.data.db.favouriteTracks

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
    @PrimaryKey val trackId: Int,
    @ColumnInfo(name = "track_name") val trackName: String,
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "track_time_millis") val trackTimeMillis: Long,
    @ColumnInfo(name = "artwork_url") val artworkUrl: String,
    @ColumnInfo(name = "collection_name") val collectionName: String?,
    @ColumnInfo(name = "release_date") val releaseDate: String?,
    @ColumnInfo(name = "genre") val genre: String?,
    @ColumnInfo(name = "country") val country: String?,
    @ColumnInfo(name = "preview_url") val previewUrl: String?,
    @ColumnInfo(name = "added_timestamp") val addedTimestamp: Long = System.currentTimeMillis()
)