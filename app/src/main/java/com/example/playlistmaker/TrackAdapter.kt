package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TrackAdapter(
    private var tracks: List<Track>,
    private val onTrackClick: (Track) -> Unit = {}
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {


    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameView: TextView = itemView.findViewById(R.id.track_name)
        private val artistNameView: TextView = itemView.findViewById(R.id.artist_name)
        private val trackTimeView: TextView = itemView.findViewById(R.id.track_time)
        private val artworkUrl100View: ImageView = itemView.findViewById(R.id.item_image)
        fun bind(model: Track) {
            trackNameView.text = model.trackName
            artistNameView.text = model.artistName
            trackTimeView.text = model.trackTime

            Glide.with(itemView.context)
                .load(model.artworkUrl100)
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

    override fun getItemCount(): Int = tracks.size

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)

        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }
    }

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}