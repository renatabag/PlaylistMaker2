package com.example.playlistmaker.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.TrackUtils
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.ui.states.TrackUi

class TrackAdapter(
    private var tracks: List<TrackUi>,
    private val onTrackClick: (TrackUi) -> Unit = {}
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameView: TextView = itemView.findViewById(R.id.track_name)
        private val artistNameView: TextView = itemView.findViewById(R.id.artist_name)
        private val trackTimeView: TextView = itemView.findViewById(R.id.track_time)
        private val artworkUrl100View: ImageView = itemView.findViewById(R.id.item_image)

        fun bind(model: TrackUi) {
            trackNameView.text = model.trackName
            artistNameView.text = model.artistName
            trackTimeView.text = TrackUtils.formatTrackTime(model.trackTimeMillis)
            Glide.with(itemView.context)
                .load(model.artworkUrl)
                .placeholder(R.drawable.placeholder_track)
                .error(R.drawable.error)
                .centerCrop()
                .into(artworkUrl100View)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateTracks(newTracks: List<TrackUi>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}

