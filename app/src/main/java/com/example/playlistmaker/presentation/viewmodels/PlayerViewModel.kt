package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default)
    val playerState: StateFlow<PlayerState> = _playerState

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    fun preparePlayer(track: Track) {
        track.previewUrl?.let { url ->
            viewModelScope.launch {
                playerInteractor.prepare(url).collect { state ->
                    _playerState.value = state
                }
            }
        }
    }

    fun playbackControl() {
        when (_playerState.value) {
            is PlayerState.Playing -> pausePlayer()
            else -> playPlayer()
        }
    }

    private fun playPlayer() {
        playerInteractor.play()
        startProgressUpdates()
    }

    private fun pausePlayer() {
        playerInteractor.pause()
    }

    private fun startProgressUpdates() {
        viewModelScope.launch {
            while (playerInteractor.isPlaying()) {
                _currentPosition.value = playerInteractor.getCurrentPosition()
                kotlinx.coroutines.delay(PROGRESS_UPDATE_DELAY)
            }
        }
    }

    fun releasePlayer() {
        playerInteractor.release()
    }

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }
}