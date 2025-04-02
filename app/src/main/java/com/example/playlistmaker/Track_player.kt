package com.example.playlistmaker

import Track
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class Track_player : AppCompatActivity() {

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val UPDATE_INTERVAL = 1000L
    }

    private var playerState = STATE_DEFAULT
    private lateinit var play: ImageButton
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var track: Track
    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeRunnable: Runnable? = null
    private var trackDuration: Long = 0
    private var startTime: Long = 0
    private var elapsedTimeBeforePause: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.track_player)

        track = intent.getParcelableExtra<Track>("TRACK") ?: run {
            finish()
            return
        }

        displayTrackDetails(track)
        trackDuration = track.trackTimeMillis ?: 0

        val backButton = findViewById<ImageButton>(R.id.menu_button)
        backButton.setOnClickListener {
            handleBackPressed()
        }

        play = findViewById(R.id.pause)
        play.isEnabled = false

        mediaPlayer = MediaPlayer()
        preparePlayer()

        play.setOnClickListener { playbackControl() }
    }

    private fun handleBackPressed() {
        if (playerState == STATE_PLAYING) {
            pausePlayer()
            handler.postDelayed({
                finish()
            }, 100)
        } else {
            finish()
        }
    }

    private fun preparePlayer() {
        try {
            val url = track.previewUrl ?: run {
                play.isEnabled = false
                return
            }

            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                play.isEnabled = true
                playerState = STATE_PREPARED
                play.setImageResource(R.drawable.pause)
                trackDuration = mediaPlayer.duration.toLong()

                findViewById<TextView>(R.id.track_time_now).text = "00:00"
            }
            mediaPlayer.setOnCompletionListener {
                playerState = STATE_PREPARED
                play.setImageResource(R.drawable.pause)
                stopTimer()

                findViewById<TextView>(R.id.track_time_now).text = "00:00"
                elapsedTimeBeforePause = 0
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                play.isEnabled = false
                playerState = STATE_DEFAULT
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            play.isEnabled = false
        }
    }

    private fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis() - elapsedTimeBeforePause

        updateTimeRunnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                findViewById<TextView>(R.id.track_time_now).text = Track.formatTrackTime(elapsed)

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

    private fun startPlayer() {
        mediaPlayer.seekTo(elapsedTimeBeforePause.toInt())
        mediaPlayer.start()
        playerState = STATE_PLAYING
        play.setImageResource(R.drawable.play)
        startTimer()
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playerState = STATE_PAUSED
        play.setImageResource(R.drawable.pause)
        stopTimer()
        elapsedTimeBeforePause = System.currentTimeMillis() - startTime
    }

    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        mediaPlayer.release()
    }

    private fun displayTrackDetails(track: Track) {
        findViewById<TextView>(R.id.track_name).text = track.trackName
        findViewById<TextView>(R.id.artist_name).text = track.artistName
        findViewById<TextView>(R.id.track_time).text = Track.formatTrackTime(track.trackTimeMillis ?: 0)
        findViewById<TextView>(R.id.track_time_now).text = "00:00"
        findViewById<TextView>(R.id.year_label_text).text = track.getReleaseYear()
        findViewById<TextView>(R.id.genre_label_text).text = track.primaryGenreName
        findViewById<TextView>(R.id.country_label_text).text = track.country

        val artworkUrl = track.artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg")
        Glide.with(this)
            .load(artworkUrl)
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.error)
            .centerCrop()
            .into(findViewById(R.id.item_image))
    }
}