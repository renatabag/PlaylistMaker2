package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TrackAdapter(
    private val tracks: List<Track>,
    private val onTrackClickListener: OnItemClickListener
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(track: Track)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
        holder.itemView.setOnClickListener {
            onTrackClickListener.onItemClick(tracks[holder.adapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameView: TextView = itemView.findViewById(R.id.trackName)
        private val artistNameView: TextView = itemView.findViewById(R.id.artistName)
        private val trackTimeView: TextView = itemView.findViewById(R.id.trackTime)
        private val trackImageView: ImageView = itemView.findViewById(R.id.item_image)

        fun bind(model: Track) {
            trackNameView.text = model.trackName
            artistNameView.text = model.artistName
            trackTimeView.text = model.trackTime

            Glide.with(itemView.context)
                .load(model.artworkUrl100)
                .placeholder(R.drawable.placeholder)
                .into(trackImageView)
        }
    }
}