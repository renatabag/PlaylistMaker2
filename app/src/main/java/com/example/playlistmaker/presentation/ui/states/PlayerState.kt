package com.example.playlistmaker.presentation.ui.states

sealed class PlayerState {
    data class Default(
        val position: Long = 0L,
        val isPlaying: Boolean = false
    ) : PlayerState()

    data class Prepared(
        val position: Long = 0L,
        val isPlaying: Boolean = false
    ) : PlayerState()

    data class Playing(
        val position: Long,
        val isPlaying: Boolean = true
    ) : PlayerState()

    data class Paused(
        val position: Long,
        val isPlaying: Boolean = false
    ) : PlayerState()

    data class Progress(
        val position: Long,
        val isPlaying: Boolean = false
    ) : PlayerState()

    data class Complete(
        val position: Long = 0L,
        val isPlaying: Boolean = false
    ) : PlayerState()

    data class Error(
        val message: String,
        val extra: Int,
        val position: Long = 0L,
        val isPlaying: Boolean = false
    ) : PlayerState()
}