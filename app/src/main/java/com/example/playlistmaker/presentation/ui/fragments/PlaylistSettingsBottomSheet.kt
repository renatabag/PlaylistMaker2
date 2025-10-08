package com.example.playlistmaker.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.playlistmaker.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

interface PlaylistSettingsListener {
    fun onSharePlaylist()
    fun onEditPlaylist()
    fun onDeletePlaylist()
}

class PlaylistSettingsBottomSheet : BottomSheetDialogFragment() {

    private var listener: PlaylistSettingsListener? = null

    companion object {
        const val TAG = "PlaylistSettingsBottomSheet"
        private const val ARG_PLAYLIST_NAME = "playlist_name"
        private const val ARG_TRACK_COUNT = "track_count"
        private const val ARG_PLAYLIST_ID = "playlist_id"
        private const val ARG_PLAYLIST_DESCRIPTION = "playlist_description"

        fun newInstance(
            playlistId: Long,
            playlistName: String,
            trackCount: Int,
            description: String? = null
        ): PlaylistSettingsBottomSheet {
            val fragment = PlaylistSettingsBottomSheet()
            val args = Bundle().apply {
                putLong(ARG_PLAYLIST_ID, playlistId)
                putString(ARG_PLAYLIST_NAME, playlistName)
                putInt(ARG_TRACK_COUNT, trackCount)
                putString(ARG_PLAYLIST_DESCRIPTION, description)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_information, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPlaylistInfo(view)
        setupClickListeners(view)
    }

    private fun setupPlaylistInfo(view: View) {
        val playlistName = arguments?.getString(ARG_PLAYLIST_NAME) ?: ""
        val trackCount = arguments?.getInt(ARG_TRACK_COUNT) ?: 0
        val description = arguments?.getString(ARG_PLAYLIST_DESCRIPTION)

        // Устанавливаем название плейлиста
        view.findViewById<TextView>(R.id.playlist_title).text = playlistName

        // Устанавливаем количество треков
        val trackCountText = formatTrackCount(trackCount)
        view.findViewById<TextView>(R.id.playlist_track_count).text = trackCountText
    }

    private fun formatTrackCount(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "$count трек"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
            else -> "$count треков"
        }
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<View>(R.id.share_playlist_button).setOnClickListener {
            listener?.onSharePlaylist()
            dismiss()
        }

        view.findViewById<View>(R.id.edit_information_button).setOnClickListener {
            listener?.onEditPlaylist()
            dismiss()
        }

        // УБИРАЕМ вызов диалогового окна и сразу вызываем удаление
        view.findViewById<View>(R.id.delete_playlist_button).setOnClickListener {
            listener?.onDeletePlaylist()
            dismiss()
        }
    }

    // УДАЛЯЕМ метод showDeleteConfirmationDialog()

    fun setListener(listener: Any) {
        this.listener = listener as PlaylistSettingsListener?
    }
}