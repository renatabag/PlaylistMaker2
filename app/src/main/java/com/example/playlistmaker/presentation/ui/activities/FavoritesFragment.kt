package com.example.playlistmaker.presentation.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentTracksBinding
import com.example.playlistmaker.presentation.ui.adapters.TrackAdapter
import com.example.playlistmaker.presentation.ui.states.FavouritesState
import com.example.playlistmaker.presentation.ui.states.TrackUi
import com.example.playlistmaker.presentation.viewmodels.FavoritesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentTracksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModel()
    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(emptyList()) { trackUi ->
            val bundle = Bundle().apply {
                putParcelable("track", trackUi)
            }
            findNavController().navigate(R.id.track_player, bundle)
        }

        binding.playlistsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistsRecycler.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoritesState.collect { state ->
                    when (state) {
                        FavouritesState.Empty -> showEmptyState()
                        is FavouritesState.Content -> showContent(state.tracks)
                    }
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.playlistsRecycler.visibility = View.GONE
        binding.emptyMediaStateImage.visibility = View.VISIBLE
        binding.emptyMediaState.visibility = View.VISIBLE
        binding.emptyMediaState.text = getString(R.string.empty_medialab)
    }

    private fun showContent(tracks: List<TrackUi>) {
        binding.playlistsRecycler.visibility = View.VISIBLE
        binding.emptyMediaStateImage.visibility = View.GONE
        binding.emptyMediaState.visibility = View.GONE
        adapter.updateTracks(tracks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}