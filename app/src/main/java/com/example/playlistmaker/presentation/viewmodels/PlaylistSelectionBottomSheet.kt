package com.example.playlistmaker.presentation.ui.viewmodels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.ui.activities.NewPlaylistFragment
import com.example.playlistmaker.presentation.ui.adapters.PlaylistSelectionAdapter
import com.example.playlistmaker.presentation.ui.states.TrackUi
import com.example.playlistmaker.presentation.viewmodels.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistSelectionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var playlistSelectionAdapter: PlaylistSelectionAdapter
    private val viewModel: PlayerViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.playlist_selection_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val track = arguments?.getParcelable<TrackUi>("track")

        playlistSelectionAdapter = PlaylistSelectionAdapter { playlist ->
            track?.let {
                viewModel.addTrackToPlaylist(playlist, it)
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.playlists_recycler_view)
        recyclerView.apply {
            adapter = playlistSelectionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        view.findViewById<Button>(R.id.add_to_album_).setOnClickListener {
            dismiss()
            openNewPlaylistScreen()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.addToPlaylistStatus.collect { status ->
                    when (status) {
                        is PlayerViewModel.AddToPlaylistStatus.Success -> {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Добавлено в плейлист \"${status.playlistName}\"",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                            viewModel.resetAddToPlaylistStatus()
                        }
                        is PlayerViewModel.AddToPlaylistStatus.AlreadyExists -> {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Трек уже добавлен в плейлист \"${status.playlistName}\"",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            viewModel.resetAddToPlaylistStatus()
                        }
                        is PlayerViewModel.AddToPlaylistStatus.Error -> {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Ошибка добавления в плейлист",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            viewModel.resetAddToPlaylistStatus()
                        }
                        else -> {}
                    }
                }
            }
        }

        // Наблюдаем за списком плейлистов
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.playlists.collect { playlists ->
                    playlistSelectionAdapter.submitList(playlists)
                }
            }
        }

        // Загружаем плейлисты
        viewModel.loadPlaylists()
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(R.id.playlists_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                behavior.isHideable = false
                behavior.isFitToContents = false

                val displayMetrics = resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels
                behavior.peekHeight = (screenHeight * 0.8).toInt()
            }
        }
    }

    private fun openNewPlaylistScreen() {
        val track = arguments?.getParcelable<TrackUi>("track")

        try {
            findNavController().navigate(
                R.id.newPlaylistFragment,
                Bundle().apply {
                    putParcelable("track", track)
                }
            )
        } catch (e: Exception) {
        }

        dismiss()
    }

    companion object {
        fun newInstance(track: TrackUi): PlaylistSelectionBottomSheet {
            return PlaylistSelectionBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable("track", track)
                }
            }
        }
    }

}