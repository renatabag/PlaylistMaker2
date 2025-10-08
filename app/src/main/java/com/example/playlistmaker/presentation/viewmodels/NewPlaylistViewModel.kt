package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.presentation.ui.states.TrackUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlistForEditing = MutableStateFlow<Playlist?>(null)
    val playlistForEditing = _playlistForEditing.asStateFlow()

    private val _creationSuccess = MutableStateFlow(false)
    val creationSuccess = _creationSuccess.asStateFlow()

    private var createdPlaylistId: Long = -1L

    fun loadPlaylistForEditing(playlistId: Long) {
        viewModelScope.launch {
            val playlistEntity = playlistInteractor.getPlaylistById(playlistId)
            playlistEntity?.let { entity ->
                _playlistForEditing.value = Playlist(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    coverImagePath = entity.coverImagePath,
                    trackCount = entity.trackCount,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    fun createPlaylist(name: String, description: String?, coverImagePath: String?) {
        viewModelScope.launch {
            try {
                val playlistId = playlistInteractor.createPlaylist(name, description, coverImagePath)
                createdPlaylistId = playlistId
                _creationSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePlaylist(playlistId: Long, name: String, description: String?, coverImagePath: String?) {
        viewModelScope.launch {
            try {
                val existingPlaylist = playlistInteractor.getPlaylistById(playlistId)
                existingPlaylist?.let { entity ->
                    val updatedEntity = entity.copy(
                        name = name,
                        description = description,
                        coverImagePath = coverImagePath,
                        updatedAt = System.currentTimeMillis()
                    )
                    val success = playlistInteractor.updatePlaylist(updatedEntity)
                    if (success) {
                        _creationSuccess.value = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addTrackToCreatedPlaylist(track: TrackUi) {
        viewModelScope.launch {
            if (createdPlaylistId != -1L) {
                try {
                    // Сохраняем трек в базу данных
                    val trackEntity = com.example.playlistmaker.data.db.playlist.PlaylistTrackEntity(
                        trackId = track.trackId.toLong(),
                        trackName = track.trackName,
                        artistName = track.artistName,
                        trackTimeMillis = track.trackTimeMillis,
                        artworkUrl = track.artworkUrl,
                        collectionName = track.collectionName,
                        releaseDate = track.releaseDate,
                        genre = track.genre,
                        country = track.country,
                        previewUrl = track.previewUrl
                    )
                    playlistInteractor.savePlaylistTrack(trackEntity)

                    // Добавляем трек в плейлист
                    playlistInteractor.addTrackToPlaylist(createdPlaylistId, track.trackId.toLong())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}