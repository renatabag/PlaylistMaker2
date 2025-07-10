package com.example.playlistmaker.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl: String,
    val collectionName: String?,
    val releaseDate: String?,
    val genre: String?,
    val country: String?,
    val previewUrl: String?
) : Parcelable {
    fun getReleaseYear(): String? = releaseDate?.take(4)

    companion object {
        fun formatTrackTime(millis: Long): String {
            val minutes = millis / 1000 / 60
            val seconds = millis / 1000 % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}