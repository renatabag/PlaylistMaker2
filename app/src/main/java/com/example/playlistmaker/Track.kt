package com.example.playlistmaker

data class Track(
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?
) {
    val trackTime: String
        get() = formatTrackTime(trackTimeMillis ?: 0)

    companion object {
        fun formatTrackTime(millis: Long): String {
            val minutes = millis / 1000 / 60
            val seconds = millis / 1000 % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}