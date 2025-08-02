package com.example.playlistmaker.presentation.ui.states

import android.os.Parcel
import android.os.Parcelable
import com.example.playlistmaker.domain.models.Track

data class TrackUi(
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
) : Parcelable {
    fun getReleaseYear(): String? = releaseDate?.take(4)

    fun getArtworkUrl512(): String = artworkUrl.replaceAfterLast('/', "512x512bb.jpg")

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(trackId)
        dest.writeString(trackName)
        dest.writeString(artistName)
        dest.writeLong(trackTimeMillis)
        dest.writeString(artworkUrl)
        dest.writeString(collectionName)
        dest.writeString(releaseDate)
        dest.writeString(genre)
        dest.writeString(country)
        dest.writeString(previewUrl)
    }

    companion object CREATOR : Parcelable.Creator<TrackUi> {
        override fun createFromParcel(parcel: Parcel): TrackUi {
            return TrackUi(
                trackId = parcel.readInt(),
                trackName = parcel.readString() ?: "",
                artistName = parcel.readString() ?: "",
                trackTimeMillis = parcel.readLong(),
                artworkUrl = parcel.readString() ?: "",
                collectionName = parcel.readString(),
                releaseDate = parcel.readString(),
                genre = parcel.readString(),
                country = parcel.readString(),
                previewUrl = parcel.readString()
            )
        }

        override fun newArray(size: Int): Array<TrackUi?> = arrayOfNulls(size)

        fun fromDomain(track: TrackUi): TrackUi {
            return TrackUi(
                track.trackId,
                track.trackName,
                track.artistName,
                track.trackTimeMillis,
                track.artworkUrl,
                track.collectionName,
                track.releaseDate,
                track.genre,
                track.country,
                track.previewUrl
            )
        }

        fun toDomain(trackUi: TrackUi): Track {
            return Track(
                trackUi.trackId,
                trackUi.trackName,
                trackUi.artistName,
                trackUi.trackTimeMillis,
                trackUi.artworkUrl,
                trackUi.collectionName,
                trackUi.releaseDate,
                trackUi.genre,
                trackUi.country,
                trackUi.previewUrl
            )
        }

    }
}