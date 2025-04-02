package com.example.playlistmaker

import Track

data class TrackResponse(
    val resultCount: Int,
    val results: List<Track>
)