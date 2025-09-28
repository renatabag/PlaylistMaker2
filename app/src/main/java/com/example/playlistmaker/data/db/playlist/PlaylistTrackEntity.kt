package com.example.playlistmaker.data.db.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "playlist_tracks")
data class PlaylistTrackEntity(
    @PrimaryKey val trackId: Long,
    @ColumnInfo(name = "track_name") val trackName: String,
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "track_time_millis") val trackTimeMillis: Long,
    @ColumnInfo(name = "artwork_url") val artworkUrl: String?,
    @ColumnInfo(name = "collection_name") val collectionName: String?,
    @ColumnInfo(name = "release_date") val releaseDate: String?,
    @ColumnInfo(name = "genre") val genre: String?,
    @ColumnInfo(name = "country") val country: String?,
    @ColumnInfo(name = "preview_url") val previewUrl: String?
)