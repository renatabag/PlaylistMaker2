package com.example.playlistmaker.domain.models

sealed class PlayerState {
    object Default : PlayerState()
    object Prepared : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    data class Progress(val position: Long) : PlayerState()
    object Complete : PlayerState()
    data class Error(val message: String, val extra: Int) : PlayerState()
}