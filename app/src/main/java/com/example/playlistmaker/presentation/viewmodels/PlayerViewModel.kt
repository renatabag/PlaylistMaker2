package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.presentation.ui.states.PlayerState
import com.example.playlistmaker.presentation.ui.states.TrackUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default(0L))
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun preparePlayer(track: TrackUi) {
        track.previewUrl?.let { url ->
            viewModelScope.launch {
                playerInteractor.prepare(url).collect { state ->
                    _playerState.value = state
                }
            }
        }
    }

    fun playbackControl() {
        when (val currentState = _playerState.value) {
            is PlayerState.Prepared -> playPlayer()
            is PlayerState.Playing -> pausePlayer()
            is PlayerState.Paused -> playPlayer()
            else -> {} // Для других состояний ничего не делаем
        }
    }

    private fun playPlayer() {
        playerInteractor.play()
        _playerState.value = PlayerState.Playing(playerInteractor.getCurrentPosition())
        startProgressUpdates()
    }

    fun pausePlayer() {
        playerInteractor.pause()
        _playerState.value = PlayerState.Paused(playerInteractor.getCurrentPosition())
    }

    private fun startProgressUpdates() {
        viewModelScope.launch {
            while (_playerState.value is PlayerState.Playing) {
                val currentPos = playerInteractor.getCurrentPosition()
                _playerState.value = PlayerState.Playing(currentPos)
                kotlinx.coroutines.delay(PROGRESS_UPDATE_DELAY)
            }
        }
    }

    fun releasePlayer() {
        playerInteractor.release()
        _playerState.value = PlayerState.Default(0L)
    }

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }
}