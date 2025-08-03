package com.example.playlistmaker.presentation.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentTracksBinding
import com.example.playlistmaker.presentation.viewmodels.TracksViewModel
import org.koin.android.ext.android.inject

class TracksFragment : Fragment() {
    private val viewModel: TracksViewModel by inject()
    companion object {
        fun newInstance() = TracksFragment()
    }

    private var _binding: FragmentTracksBinding? = null
    private val binding get() = _binding!!

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

        // Настройка состояния пустого списка
        binding.emptyMediaState.text = getString(R.string.empty_medialab) // Используем строковый ресурс
        binding.emptyMediaStateImage.setImageResource(R.drawable.empty)

        showEmptyState(true)
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            binding.playlistsRecycler.visibility = View.GONE
            binding.emptyMediaStateImage.visibility = View.VISIBLE
            binding.emptyMediaState.visibility = View.VISIBLE
        } else {
            binding.playlistsRecycler.visibility = View.VISIBLE
            binding.emptyMediaStateImage.visibility = View.GONE
            binding.emptyMediaState.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}