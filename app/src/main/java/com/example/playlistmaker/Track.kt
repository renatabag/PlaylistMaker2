package com.example.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

data class Track (val trackName: String, val artistName: String, val trackTime: String, val artworkUrl100: String){
    companion object {
        fun formatTrackTime(milliseconds: Long): String {
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            return dateFormat.format(milliseconds)
        }
    }
}