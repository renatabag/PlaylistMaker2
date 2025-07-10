package com.example.playlistmaker.domain

object TrackUtils {
    fun formatTrackTime(millis: Long): String {
        val minutes = millis / 1000 / 60
        val seconds = millis / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}