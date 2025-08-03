package com.example.playlistmaker.presentation.ui.activities

import android.annotation.SuppressLint
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
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.presentation.ui.states.PlayerState
import com.example.playlistmaker.presentation.ui.states.TrackUi

class TrackPlayer : AppCompatActivity() {
    private val playerInteractor by lazy { Creator.providePlayerInteractor() }
    private val viewModel by lazy { PlayerViewModel(playerInteractor) }
    private lateinit var track: TrackUi

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.track_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.track_player)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

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
            when (val state = viewModel.playerState.value) {
                is PlayerState.Playing -> viewModel.pausePlayer()
                else -> {} // Ничего не делаем для других состояний
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
                        is PlayerState.Prepared -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = true
                            findViewById<TextView>(R.id.track_time_now).text =
                                TrackUtils.formatTrackTime(state.position)
                            findViewById<ImageButton>(R.id.pause).setImageResource(R.drawable.pause)
                        }
                        is PlayerState.Playing -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = true
                            findViewById<ImageButton>(R.id.pause).setImageResource(R.drawable.play)
                            findViewById<TextView>(R.id.track_time_now).text =
                                TrackUtils.formatTrackTime(state.position)
                        }
                        is PlayerState.Paused -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = true
                            findViewById<ImageButton>(R.id.pause).setImageResource(R.drawable.pause)
                            findViewById<TextView>(R.id.track_time_now).text =
                                TrackUtils.formatTrackTime(state.position)
                        }
                        is PlayerState.Error -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = false
                        }
                        is PlayerState.Default -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = false
                            findViewById<TextView>(R.id.track_time_now).text = "00:00"
                        }
                        is PlayerState.Progress -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = false
                            findViewById<TextView>(R.id.track_time_now).text =
                                TrackUtils.formatTrackTime(state.position)
                        }
                        is PlayerState.Complete -> {
                            findViewById<ImageButton>(R.id.pause).isEnabled = false
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        when (viewModel.playerState.value) {
            is PlayerState.Playing -> viewModel.pausePlayer()
            else -> {}
        }
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