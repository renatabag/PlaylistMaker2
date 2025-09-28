package com.example.playlistmaker.domain

import android.content.Context
import com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity
import com.example.playlistmaker.presentation.ui.states.TrackUi

object TrackUtils {
    fun formatTrackTime(millis: Long): String {
        val minutes = millis / 1000 / 60
        val seconds = millis / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
fun dpToPx(context: Context, dp: Int): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}
fun TrackUi.toPlaylistTrackEntity(): PlaylistTrackEntity {
    return PlaylistTrackEntity(
        trackId = this.trackId.toLong(),
        trackName = this.trackName,
        artistName = this.artistName,
        trackTimeMillis = this.trackTimeMillis,
        artworkUrl = this.artworkUrl,
        collectionName = this.collectionName,
        releaseDate = this.releaseDate,
        genre = this.genre,
        country = this.country,
        previewUrl = this.previewUrl
    )
}