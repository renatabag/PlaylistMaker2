package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.interactors.PlaylistTracksInteractor
import com.example.playlistmaker.presentation.ui.states.PlaylistState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistTracksInteractor: PlaylistTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlistState = MutableStateFlow<PlaylistState>(PlaylistState.Loading)
    val playlistState: StateFlow<PlaylistState> = _playlistState.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                _playlistState.value = PlaylistState.Loading
                val playlistWithTracks = playlistTracksInteractor.getPlaylistWithTracks(playlistId)

                // Проверяем, есть ли треки в плейлисте
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
                // Перезагружаем плейлист после удаления
                loadPlaylist(playlistId)
            } catch (e: Exception) {
                _playlistState.value = PlaylistState.Error("Ошибка при удалении трека: ${e.message}")
            }
        }
    }
    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                // Используем playlistInteractor для удаления
                playlistInteractor.deletePlaylistById(playlistId)

                println("Плейлист $playlistId успешно удален")
            } catch (e: Exception) {
                _playlistState.value = PlaylistState.Error("Ошибка удаления плейлиста: ${e.message}")
                println("Ошибка удаления плейлиста: ${e.message}")
            }
        }
    }
}