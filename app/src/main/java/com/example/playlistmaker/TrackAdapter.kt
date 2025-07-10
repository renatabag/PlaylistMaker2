package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TrackAdapter(private val tracks: List<Track>): RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {
    class TrackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val trackNameView:TextView
        private val artistNameView:TextView
        private val trackTimeView: TextView
        private val artworkUrl100View: ImageView

        init {
            trackNameView = itemView.findViewById(R.id.trackName)
            artistNameView = itemView.findViewById(R.id.artistName)
            trackTimeView = itemView.findViewById(R.id.trackTime)
            artworkUrl100View = itemView.findViewById(R.id.item_image)
        }

        fun bind(model: Track) {
            trackNameView.text = model.trackName
            artistNameView.text = model.artistName
            trackTimeView.text=model.trackTime

            Glide.with(itemView.context)
                .load(model.artworkUrl100)
                .placeholder(R.drawable.placeholder)
                .into(artworkUrl100View)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent,false)
        return TrackViewHolder(view)
    }

    override fun getItemCount()=tracks.size

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }
}
