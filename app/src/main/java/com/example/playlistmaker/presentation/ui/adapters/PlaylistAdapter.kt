package com.example.playlistmaker.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.domain.dpToPx

class PlaylistAdapter(
    private val onPlaylistClick: (PlaylistEntity) -> Unit
) : ListAdapter<PlaylistEntity, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverImage: ImageView = itemView.findViewById(R.id.playlist_image)
        private val titleText: TextView = itemView.findViewById(R.id.playlist_title)
        private val trackCountText: TextView = itemView.findViewById(R.id.playlist_track_count)

        fun bind(playlist: PlaylistEntity) {
            titleText.text = playlist.name // Убедитесь, что здесь правильное название

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
                    .transform(RoundedCorners(dpToPx(itemView.context, 16).toInt()))
                    .into(coverImage)
            } ?: run {
                // Установка placeholder, если нет изображения
                coverImage.setImageResource(R.drawable.placeholder_track)
            }

            itemView.setOnClickListener {
                onPlaylistClick(playlist)
            }
        }
    }

    companion object {
        private val PlaylistDiffCallback = object : DiffUtil.ItemCallback<PlaylistEntity>() {
            override fun areItemsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}