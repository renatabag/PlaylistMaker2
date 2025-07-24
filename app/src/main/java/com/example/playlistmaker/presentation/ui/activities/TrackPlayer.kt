package com.example.playlistmaker.presentation.ui.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.PlayerController
import com.example.playlistmaker.domain.PlayerController.Companion.STATE_DEFAULT
import com.example.playlistmaker.domain.PlayerController.PlayerStateListener
import com.example.playlistmaker.domain.TrackUtils
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.viewmodels.ParcelableTrack

class TrackPlayer : AppCompatActivity(), PlayerController.PlayerStateListener {
    private lateinit var playerController: PlayerController
    private lateinit var track: Track
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null
    private var updateTimeRunnable: Runnable? = null
    private var listener: PlayerStateListener? = null
    private var startTime: Long = 0
    private var elapsedTimeBeforePause: Long = 0
    private var playerState = STATE_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.track_player)

        val parcelableTrack = intent.getParcelableExtra<ParcelableTrack>("TRACK")
        track = parcelableTrack?.track ?: run {
            Toast.makeText(this, "Ошибка: трек не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        playerController = PlayerController()
        displayTrackDetails(track)

        findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            if (playerController.isPlaying()) {
                playerController.pause()
                handler.postDelayed({ finish() }, 100)
            } else {
                finish()
            }
        }

        val playButton = findViewById<ImageButton>(R.id.pause).apply {
            isEnabled = false
            setOnClickListener { playerController.playbackControl() }
        }

        track.previewUrl?.let { url ->
            playerController.prepare(url, this)
        } ?: run {
            playButton.isEnabled = false
            Toast.makeText(this, "Превью трека недоступно", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPrepared() {
        findViewById<ImageButton>(R.id.pause).isEnabled = true
        findViewById<TextView>(R.id.track_time_now).text = "00:00"
    }

    override fun onCompletion() {
        findViewById<ImageButton>(R.id.pause).setImageResource(R.drawable.pause)
        findViewById<TextView>(R.id.track_time_now).text = "00:00"
    }

    override fun onError(what: Int, extra: Int) {
        findViewById<ImageButton>(R.id.pause).isEnabled = false
    }

    override fun onPlaybackStarted() {
        findViewById<ImageButton>(R.id.pause).setImageResource(R.drawable.play)
    }

    override fun onPlaybackPaused() {
        findViewById<ImageButton>(R.id.pause).setImageResource(R.drawable.pause)
    }

    override fun onProgressUpdate(elapsedTime: Long) {
        findViewById<TextView>(R.id.track_time_now).text =
            TrackUtils.formatTrackTime(elapsedTime)
    }

    private fun displayTrackDetails(track: Track) {
        findViewById<TextView>(R.id.track_name).text = track.trackName
        findViewById<TextView>(R.id.artist_name).text = track.artistName
        findViewById<TextView>(R.id.track_time).text = TrackUtils.formatTrackTime(track.trackTimeMillis)
        findViewById<TextView>(R.id.track_time_now).text = "00:00"
        findViewById<TextView>(R.id.year_label_text).text = track.getReleaseYear()
        findViewById<TextView>(R.id.genre_label_text).text = track.genre
        findViewById<TextView>(R.id.country_label_text).text = track.country

        val artworkUrl = track.artworkUrl?.replace("100x100bb.jpg", "512x512bb.jpg")
        Glide.with(this)
            .load(artworkUrl)
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.error)
            .centerCrop()
            .into(findViewById(R.id.item_image))
    }

    override fun onPause() {
        super.onPause()
        if (playerController.isPlaying()) {
            playerController.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerController.release()
    }
}