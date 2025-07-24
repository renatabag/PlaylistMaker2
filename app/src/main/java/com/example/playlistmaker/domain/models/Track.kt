package com.example.playlistmaker.domain.models


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
) {
    fun getReleaseYear(): String? = releaseDate?.take(4)
    fun getArtworkUrl512(): String = artworkUrl.replaceAfterLast('/', "512x512bb.jpg")
}