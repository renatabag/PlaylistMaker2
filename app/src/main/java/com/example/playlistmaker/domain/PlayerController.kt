package com.example.playlistmaker.domain

import android.media.MediaPlayer
import android.os.Looper
import android.os.Handler

class PlayerController(
    private val handler: Handler = Handler(Looper.getMainLooper())
) {
    private var mediaPlayer: MediaPlayer? = null
    private var updateTimeRunnable: Runnable? = null
    private var listener: PlayerStateListener? = null

    private var startTime: Long = 0
    private var elapsedTimeBeforePause: Long = 0
    var playerState = STATE_DEFAULT
        private set

    fun prepare(url: String, listener: PlayerStateListener) {
        this.listener = listener
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                playerState = STATE_PREPARED
                listener.onPrepared()
            }
            setOnCompletionListener {
                playerState = STATE_PREPARED
                stopTimer()
                listener.onCompletion()
            }
            setOnErrorListener { _, what, extra ->
                playerState = STATE_DEFAULT
                listener.onError(what, extra)
                true
            }
        }
    }

    fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> pause()
            STATE_PREPARED, STATE_PAUSED -> start()
        }
    }

    private fun start() {
        mediaPlayer?.let { player ->
            player.seekTo(elapsedTimeBeforePause.toInt())
            player.start()
            playerState = STATE_PLAYING
            listener?.onPlaybackStarted()
            startTimer()
        }
    }

    internal fun pause() {
        mediaPlayer?.pause()
        playerState = STATE_PAUSED
        listener?.onPlaybackPaused()
        stopTimer()
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis() - elapsedTimeBeforePause
        updateTimeRunnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                listener?.onProgressUpdate(elapsed)
                if (playerState == STATE_PLAYING) {
                    handler.postDelayed(this, UPDATE_INTERVAL)
                }
            }
        }
        handler.post(updateTimeRunnable!!)
    }

    private fun stopTimer() {
        elapsedTimeBeforePause = System.currentTimeMillis() - startTime
        updateTimeRunnable?.let {
            handler.removeCallbacks(it)
            updateTimeRunnable = null
        }
    }

    fun release() {
        stopTimer()
        mediaPlayer?.release()
        mediaPlayer = null
        listener = null
    }

    interface PlayerStateListener {
        fun onPrepared()
        fun onCompletion()
        fun onError(what: Int, extra: Int)
        fun onPlaybackStarted()
        fun onPlaybackPaused()
        fun onProgressUpdate(elapsedTime: Long)
    }
    fun isPlaying(): Boolean {
        return playerState == STATE_PLAYING
    }

    companion object {
        const val STATE_DEFAULT = 0
        const val STATE_PREPARED = 1
        const val STATE_PLAYING = 2
        const val STATE_PAUSED = 3
        private const val UPDATE_INTERVAL = 1000L
    }
}