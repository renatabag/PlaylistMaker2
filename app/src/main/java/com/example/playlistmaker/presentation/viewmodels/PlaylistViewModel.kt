package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.interactors.PlaylistTracksInteractor
import com.example.playlistmaker.presentation.ui.states.PlaylistState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistTracksInteractor: PlaylistTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlistState = MutableStateFlow<PlaylistState>(PlaylistState.Loading)
    val playlistState: StateFlow<PlaylistState> = _playlistState.asStateFlow()

    private val _playlistDeletionStatus = MutableStateFlow(false)
    val playlistDeletionStatus: StateFlow<Boolean> = _playlistDeletionStatus.asStateFlow()

    // Добавьте SharedFlow для уведомления об обновлении списка плейлистов
    private val _playlistListUpdate = MutableSharedFlow<Unit>()
    val playlistListUpdate: SharedFlow<Unit> = _playlistListUpdate.asSharedFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                _playlistState.value = PlaylistState.Loading
                val playlistWithTracks = playlistTracksInteractor.getPlaylistWithTracks(playlistId)

                if (playlistWithTracks.tracks.isEmpty()) {
                    _playlistState.value = PlaylistState.Empty(playlist = playlistWithTracks.playlist)
                } else {
                    _playlistState.value = PlaylistState.Content(
                        playlist = playlistWithTracks.playlist,
                        tracks = playlistWithTracks.tracks
                    )
                }
            } catch (e: Exception) {
                _playlistState.value = PlaylistState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                playlistTracksInteractor.removeTrackFromPlaylist(playlistId, trackId)
                loadPlaylist(playlistId)
            } catch (e: Exception) {
                _playlistState.value = PlaylistState.Error("Ошибка при удалении трека: ${e.message}")
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                println("VIEWMODEL: Начало удаления плейлиста ID: $playlistId")
                playlistInteractor.deletePlaylist(playlistId)
                println("VIEWMODEL: Плейлист успешно удален из базы данных")
                _playlistDeletionStatus.value = true
                _playlistListUpdate.emit(Unit)
            } catch (e: Exception) {
                println("VIEWMODEL: Ошибка при удалении плейлиста: ${e.message}")
                e.printStackTrace()
                _playlistDeletionStatus.value = false
                _playlistState.value = PlaylistState.Error("Ошибка при удалении плейлиста: ${e.message}")
            }
        }
    }

}