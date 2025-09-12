package com.example.playlistmaker.presentation.ui.states

sealed class PlayerState {
    data object Default : PlayerState()
    data class Prepared(val position: Long) : PlayerState()
    data class Playing(val position: Long) : PlayerState()
    data class Paused(val position: Long) : PlayerState()
    data class Progress(val position: Long) : PlayerState()
    data class Complete(val position: Long) : PlayerState()
    data class Error(val message: String, val extra: Int, val position: Long) : PlayerState()
}