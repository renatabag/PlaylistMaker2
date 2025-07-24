package com.example.playlistmaker.presentation.viewmodels

import android.os.Parcel
import android.os.Parcelable
import com.example.playlistmaker.domain.models.Track

data class ParcelableTrack(val track: Track) : Parcelable {

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(track.trackId)
        writeString(track.trackName)
        writeString(track.artistName)
        writeLong(track.trackTimeMillis)
        writeString(track.artworkUrl)
        writeString(track.collectionName)
        writeString(track.releaseDate)
        writeString(track.genre)
        writeString(track.country)
        writeString(track.previewUrl)
    }

    companion object CREATOR : Parcelable.Creator<ParcelableTrack> {
        override fun createFromParcel(parcel: Parcel) = ParcelableTrack(
            Track(
                trackId = parcel.readInt(),
                trackName = parcel.readString().orEmpty(),
                artistName = parcel.readString().orEmpty(),
                trackTimeMillis = parcel.readLong(),
                artworkUrl = parcel.readString().orEmpty(),
                collectionName = parcel.readString(),
                releaseDate = parcel.readString(),
                genre = parcel.readString(),
                country = parcel.readString(),
                previewUrl = parcel.readString()
            )
        )

        override fun newArray(size: Int): Array<ParcelableTrack?> = arrayOfNulls(size)
    }
}
