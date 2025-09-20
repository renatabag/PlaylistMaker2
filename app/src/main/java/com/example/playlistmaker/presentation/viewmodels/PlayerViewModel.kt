package com.example.playlistmaker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.FavoriteTracksInteractor
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.ui.states.PlayerState
import com.example.playlistmaker.presentation.ui.states.TrackUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Default(0L))
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private var currentTrack: Track? = null

    private var timerJob: Job? = null

    fun playbackControl() {
        when (val currentState = _playerState.value) {
            is PlayerState.Prepared -> playPlayer()
            is PlayerState.Playing -> pausePlayer()
            is PlayerState.Paused -> playPlayer()
            else -> {}
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
        timerJob?.cancel()
        timerJob = null
    }

    private fun startProgressUpdates() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _playerState.value is PlayerState.Playing) {
                val currentPos = playerInteractor.getCurrentPosition()
                _playerState.value = PlayerState.Playing(currentPos)
                delay(PROGRESS_UPDATE_DELAY)
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
    fun resetPlayer() {
        playerInteractor.release()
        _playerState.value = PlayerState.Default(0L)
    }
    fun toggleFavorite() {
        val track = currentTrack ?: return
        viewModelScope.launch {
            if (_isFavorite.value) {
                favoriteTracksInteractor.removeFromFavorites(track)
                _isFavorite.value = false
            } else {
                favoriteTracksInteractor.addToFavorites(track)
                _isFavorite.value = true
            }
        }
    }
    private fun checkIsFavorite(trackId: Int) {
        viewModelScope.launch {
            _isFavorite.value = favoriteTracksInteractor.isFavorite(trackId)
        }
    }

    fun preparePlayer(track: TrackUi) {
        currentTrack = TrackUi.toDomain(track)
        checkIsFavorite(track.trackId)

        track.previewUrl?.let { url ->
            viewModelScope.launch {
                playerInteractor.prepare(url).collect { state ->
                    _playerState.value = state
                }
            }
        }
    }
}