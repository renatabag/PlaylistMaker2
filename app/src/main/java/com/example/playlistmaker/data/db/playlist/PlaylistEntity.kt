package com.example.playlistmaker.data.db.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val coverImagePath: String? = null,
    val trackIdsJson: String = "[]",
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getTrackIds(): List<Long> {
        return if (trackIdsJson.isBlank()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<Long>>() {}.type
                Gson().fromJson(trackIdsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun setTrackIds(trackIds: List<Long>): PlaylistEntity {
        val json = Gson().toJson(trackIds)
        return this.copy(
            trackIdsJson = json,
            trackCount = trackIds.size,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun addTrack(trackId: Long): PlaylistEntity {
        val currentIds = getTrackIds().toMutableList()
        if (!currentIds.contains(trackId)) {
            currentIds.add(trackId)
        }
        return setTrackIds(currentIds)
    }


    fun containsTrack(trackId: Long): Boolean {
        return getTrackIds().contains(trackId)
    }
    fun removeTrack(trackId: Long): PlaylistEntity {
        val currentIds = getTrackIds().toMutableList()
        return setTrackIds(currentIds)
    }
}