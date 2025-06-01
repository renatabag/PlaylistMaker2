package com.example.playlistmaker.data

import com.example.playlistmaker.data.dto.TrackDTO

data class TrackResponseDto(
    val resultCount: Int,
    val results: List<TrackDTO>
)