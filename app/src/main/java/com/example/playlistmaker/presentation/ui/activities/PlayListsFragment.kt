package com.example.playlistmaker.presentation.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.data.db.playlist.PlaylistEntity
import com.example.playlistmaker.databinding.FragmentListBinding
import com.example.playlistmaker.presentation.ui.adapters.PlaylistAdapter
import com.example.playlistmaker.presentation.ui.fragments.PlaylistFragment
import com.example.playlistmaker.presentation.ui.states.PlaylistsState
import com.example.playlistmaker.presentation.ui.states.TrackUi
import com.example.playlistmaker.presentation.viewmodels.PlayListsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayListsFragment : Fragment() {

    private val viewModel: PlayListsViewModel by viewModel()
    private var _binding: FragmentListBinding? = null
    private val binding: FragmentListBinding get() = _binding!!
    private lateinit var playlistAdapter: PlaylistAdapter

    companion object {
        fun newInstance() = PlayListsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        binding.emptyListState.text = getString(R.string.empty_playlist)
        binding.emptyMediaStateImage.setImageResource(R.drawable.empty)
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter { playlist ->
            // Обработка клика по плейлисту - открываем PlaylistFragment
            openPlaylistDetails(playlist.id)
        }


        binding.playlistsRecycler.apply {
            adapter = playlistAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun openPlaylistDetails(playlistId: Long) {
        val fragment = PlaylistFragment.newInstance(playlistId)
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack("playlist_details")
            .commit()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistsState.collectLatest { state ->
                when (state) {
                    is PlaylistsState.Loading -> showLoading()
                    is PlaylistsState.Empty -> showEmptyState()
                    is PlaylistsState.Content -> showPlaylists(state.playlists)
                    is PlaylistsState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.createPlaylistButton.setOnClickListener {
            openNewPlaylistScreen()
        }
    }

    private fun openNewPlaylistScreen() {
        val track = arguments?.getParcelable<TrackUi>("track")

        val fragment = NewPlaylistFragment.newInstance(track)
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack("new_playlist")
            .commit()
    }

    private fun showLoading() {
        binding.playlistsRecycler.visibility = View.GONE
        binding.emptyMediaStateImage.visibility = View.GONE
        binding.emptyListState.visibility = View.GONE
        binding.createPlaylistButton.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.playlistsRecycler.visibility = View.GONE
        binding.emptyMediaStateImage.visibility = View.VISIBLE
        binding.emptyListState.visibility = View.VISIBLE
        binding.createPlaylistButton.visibility = View.VISIBLE
    }

    private fun showPlaylists(playlists: List<PlaylistEntity>) {
        binding.playlistsRecycler.visibility = View.VISIBLE
        binding.emptyMediaStateImage.visibility = View.GONE
        binding.emptyListState.visibility = View.GONE
        binding.createPlaylistButton.visibility = View.VISIBLE

        playlistAdapter.submitList(playlists)
    }

    private fun showError(message: String) {
        showEmptyState()
        // Можно добавить Toast
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}