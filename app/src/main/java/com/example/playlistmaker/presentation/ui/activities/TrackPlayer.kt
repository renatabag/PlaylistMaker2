package com.example.playlistmaker.presentation.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.TrackUtils
import com.example.playlistmaker.presentation.viewmodels.PlayerViewModel
import kotlinx.coroutines.launch
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.impl.PlayerRepositoryImpl
import com.example.playlistmaker.presentation.ui.states.TrackUi

class TrackPlayer : AppCompatActivity() {
    private val playerRepository by lazy { PlayerRepositoryImpl() }
    private val viewModel by lazy {
        PlayerViewModel(playerRepository)
    }

    private lateinit var track: TrackUi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.track_player)

        track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("TRACK", TrackUi::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("TRACK")
        } ?: run {
            finish()
            return
        }

        displayTrackDetails(track)
        setupButtonListeners()
        setupObservers()

        track.previewUrl?.let { _ ->
            viewModel.preparePlayer(track)
        } ?: run {
            findViewById<ImageButton>(R.id.pause).isEnabled = false
        }
    }

    private fun displayTrackDetails(track: TrackUi) {
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

    private fun setupButtonListeners() {
        findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            if (viewModel.isPlaying.value == true) {
                viewModel.pausePlayer()
            }
            finish()
        }

        findViewById<ImageButton>(R.id.pause).apply {
            isEnabled = false
            setOnClickListener { viewModel.playbackControl() }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playerState.collect { state ->
                    when (state) {
                        PlayerState.Prepared -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = true
                            findViewById<TextView>(R.id.track_time_now).text = "00:00"
                        }
                        PlayerState.Playing -> {
                            findViewById<ImageButton>(R.id.pause).setImageResource(R.drawable.play)
                        }
                        PlayerState.Paused -> {
                            findViewById<ImageButton>(R.id.pause).setImageResource(R.drawable.pause)
                        }
                        is PlayerState.Error -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = false
                        }
                        PlayerState.Default -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = false
                        }
                        is PlayerState.Progress -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = false
                        }
                        PlayerState.Complete -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = false
                        }

                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPosition.collect { position ->
                    findViewById<TextView>(R.id.track_time_now).text =
                        TrackUtils.formatTrackTime(position)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
    }
    companion object {
        fun getIntent(context: Context, track: TrackUi): Intent {
            return Intent(context, TrackPlayer::class.java).apply {
                putExtra("TRACK", track)
            }
        }
    }
}