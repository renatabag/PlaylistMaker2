package com.example.playlistmaker.domain.models

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val coverImagePath: String? = null,
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)