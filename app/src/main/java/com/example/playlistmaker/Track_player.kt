package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class Track_player : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.track_player)

        val backButton = findViewById<ImageButton>(R.id.menu_button)
        backButton.setOnClickListener {
            finish()
        }

    }

    private fun displayTrackDetails(track: Track) {
        val trackNameView = findViewById<TextView>(R.id.track_name)
        val artistNameView = findViewById<TextView>(R.id.artist_name)
        val trackTimeView = findViewById<TextView>(R.id.track_time)
        val artworkView = findViewById<ImageView>(R.id.item_image)
        val collectionNameView = findViewById<TextView>(R.id.album_label_text)
        val releaseDateView = findViewById<TextView>(R.id.year_label_text)
        val genreView = findViewById<TextView>(R.id.genre_label_text)
        val countryView = findViewById<TextView>(R.id.country_label_text)

        trackNameView.text = track.trackName
        artistNameView.text = track.artistName
        trackTimeView.text = track.trackTime
        collectionNameView.text = track.collectionName
        releaseDateView.text = track.releaseDate?.toString()
        genreView.text = track.primaryGenreName
        countryView.text = track.country

        val artworkUrl = track.artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg")
        Glide.with(this)
            .load(artworkUrl)
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.error)
            .centerCrop()
            .into(artworkView)
    }
}