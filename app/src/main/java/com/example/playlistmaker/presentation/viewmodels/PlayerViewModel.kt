package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.repositories.PlayerRepository
import com.example.playlistmaker.presentation.ui.states.PlayerState
import com.example.playlistmaker.presentation.ui.states.TrackUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun preparePlayer(track: TrackUi) {
        viewModelScope.launch {
            track.previewUrl?.let { url ->
                playerRepository.prepare(url).collect { state ->
                    _playerState.value = state
                }
            }
        }
    }

    fun playPlayer() {
        playerRepository.play()
    }

    fun pausePlayer() {
        playerRepository.pause()
    }

    fun playbackControl() {
        playerRepository.playbackControl()
    }

    fun releasePlayer() {
        playerRepository.release()
    }

    fun resetPlayer() {
        releasePlayer()
        _playerState.value = PlayerState.Default
    }

    fun getCurrentPosition(): Long {
        return playerRepository.getCurrentPosition()
    }
}