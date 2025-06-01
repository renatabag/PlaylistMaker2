package com.example.playlistmaker.data.dto

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import java.util.Locale

@SuppressLint("ParcelCreator")
data class TrackDTO(
    val trackId: Int,
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(trackId)
        parcel.writeString(trackName)
        parcel.writeString(artistName)
        parcel.writeValue(trackTimeMillis)
        parcel.writeString(artworkUrl100)
        parcel.writeString(collectionName)
        parcel.writeString(releaseDate)
        parcel.writeString(primaryGenreName)
        parcel.writeString(country)
        parcel.writeString(previewUrl)
    }

    override fun describeContents(): Int = 0

    fun getReleaseYear(): String {
        return releaseDate?.take(4) ?: ""
    }
    companion object CREATOR : Parcelable.Creator<TrackDTO> {
        override fun createFromParcel(parcel: Parcel): TrackDTO {
            return TrackDTO(parcel)
        }

        override fun newArray(size: Int): Array<TrackDTO?> {
            return arrayOfNulls(size)
        }
        fun formatTrackTime(millis: Long): String {
            val totalSeconds = millis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }

    }

}