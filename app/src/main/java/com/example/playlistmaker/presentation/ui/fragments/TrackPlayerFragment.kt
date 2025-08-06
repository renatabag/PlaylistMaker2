package com.example.playlistmaker.presentation.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.TrackPlayerBinding
import com.example.playlistmaker.domain.TrackUtils
import com.example.playlistmaker.presentation.ui.states.PlayerState
import com.example.playlistmaker.presentation.ui.states.TrackUi
import com.example.playlistmaker.presentation.viewmodels.PlayerViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackPlayerFragment : Fragment() {
    private var _binding: TrackPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TrackPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val track = arguments?.getParcelable<TrackUi>(ARG_TRACK) ?: run {
            parentFragmentManager.popBackStack()
            return
        }

        viewModel.resetPlayer()
        displayTrackDetails(track)
        setupButtonListeners()
        setupObservers()

        track.previewUrl?.let {
            viewModel.preparePlayer(track)
        } ?: run {
            binding.pause.isEnabled = false
        }
    }

    private fun displayTrackDetails(track: TrackUi) {
        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName
        binding.trackTime.text = TrackUtils.formatTrackTime(track.trackTimeMillis)
        binding.trackTimeNow.text = "00:00"
        binding.yearLabelText.text = track.getReleaseYear()
        binding.genreLabelText.text = track.genre
        binding.countryLabelText.text = track.country

        val artworkUrl = track.artworkUrl?.replace("100x100bb.jpg", "512x512bb.jpg")
        Glide.with(requireContext())
            .load(artworkUrl)
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.error)
            .centerCrop()
            .into(binding.itemImage)
    }

    private fun setupButtonListeners() {
        binding.menuButton.setOnClickListener {
            // Останавливаем воспроизведение перед закрытием
            if (viewModel.playerState.value is PlayerState.Playing) {
                viewModel.pausePlayer()
            }
            parentFragmentManager.popBackStack()
        }

        binding.pause.apply {
            isEnabled = false
            setOnClickListener {
                viewModel.playbackControl()
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playerState.collect { state ->
                    when (state) {
                        is PlayerState.Prepared -> {
                            binding.pause.isEnabled = true
                            binding.trackTimeNow.text = TrackUtils.formatTrackTime(state.position)
                            binding.pause.setImageResource(R.drawable.pause)
                        }
                        is PlayerState.Playing -> {
                            binding.pause.isEnabled = true
                            binding.pause.setImageResource(R.drawable.play)
                            binding.trackTimeNow.text = TrackUtils.formatTrackTime(state.position)
                        }
                        is PlayerState.Paused -> {
                            binding.pause.isEnabled = true
                            binding.pause.setImageResource(R.drawable.pause)
                            binding.trackTimeNow.text = TrackUtils.formatTrackTime(state.position)
                        }
                        is PlayerState.Error -> {
                            binding.pause.isEnabled = false
                            binding.pause.setImageResource(R.drawable.pause)
                        }
                        is PlayerState.Default -> {
                            binding.pause.isEnabled = false
                            binding.pause.setImageResource(R.drawable.pause)
                            binding.trackTimeNow.text = "00:00"
                        }
                        is PlayerState.Progress -> {
                            binding.pause.isEnabled = false
                            binding.trackTimeNow.text = TrackUtils.formatTrackTime(state.position)
                        }
                        is PlayerState.Complete -> {
                            binding.pause.isEnabled = true
                            binding.pause.setImageResource(R.drawable.play)
                            binding.trackTimeNow.text = "00:00"
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.playerState.value is PlayerState.Playing) {
            viewModel.pausePlayer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.releasePlayer()
        _binding = null
    }

    companion object {
        private const val ARG_TRACK = "track"

        fun newInstance(track: TrackUi): TrackPlayerFragment {
            return TrackPlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TRACK, track)
                }
            }
        }
    }
}