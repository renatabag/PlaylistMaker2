package com.example.playlistmaker.data.dto

import com.example.playlistmaker.data.Track

data class TrackResponseDto(
    val resultCount: Int,
    val results: List<Track>
)