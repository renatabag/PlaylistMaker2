package com.example.playlistmaker.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.domain.dpToPx

class PlaylistSelectionAdapter(
    private val onPlaylistClick: (PlaylistEntity) -> Unit
) : RecyclerView.Adapter<PlaylistSelectionAdapter.PlaylistViewHolder>() {

    private var playlists: List<PlaylistEntity> = emptyList()

    fun submitList(playlists: List<PlaylistEntity>) {
        this.playlists = playlists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.playlist_player_item, parent, false) // Убедитесь, что это правильный layout
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverImage: ImageView = itemView.findViewById(R.id.playlist_image)
        private val titleText: TextView = itemView.findViewById(R.id.playlist_title)
        private val trackCountText: TextView = itemView.findViewById(R.id.playlist_track_count)

        fun bind(playlist: PlaylistEntity) {
            titleText.text = playlist.name

            // Форматирование количества треков
            val trackCountTextFormatted = when (playlist.trackCount) {
                0 -> "Нет треков"
                1 -> "1 трек"
                in 2..4 -> "${playlist.trackCount} трека"
                else -> "${playlist.trackCount} треков"
            }
            trackCountText.text = trackCountTextFormatted

            // Загрузка изображения обложки
            playlist.coverImagePath?.let { imagePath ->
                Glide.with(itemView)
                    .load(imagePath)
                    .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(dpToPx(itemView.context, 8).toInt()))
                    .into(coverImage)
            } ?: run {
                coverImage.setImageResource(R.drawable.placeholder_track)
            }

            itemView.setOnClickListener {
                onPlaylistClick(playlist)
            }
        }
    }
}