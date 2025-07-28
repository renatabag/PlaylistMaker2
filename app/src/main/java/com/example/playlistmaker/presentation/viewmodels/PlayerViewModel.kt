package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.interactors.impl.PlayerInteractorImpl
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.PlayerRepository
import com.example.playlistmaker.domain.repositories.impl.PlayerRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerRepositoryImpl
) : ViewModel() {

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

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
        if (_isPlaying.value) {
            pausePlayer()
        } else {
            playPlayer()
        }
    }

    private fun playPlayer() {
        playerInteractor.play()
        _isPlaying.value = true
        startProgressUpdates()
    }

    fun pausePlayer() {
        playerInteractor.pause()
        _isPlaying.value = false
    }

    private fun startProgressUpdates() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                _currentPosition.value = playerInteractor.getCurrentPosition()
                kotlinx.coroutines.delay(PROGRESS_UPDATE_DELAY)
            }
        }
    }

    fun releasePlayer() {
        playerInteractor.release()
        _isPlaying.value = false
        _currentPosition.value = 0L
        _playerState.value = PlayerState.Default
    }

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }
}