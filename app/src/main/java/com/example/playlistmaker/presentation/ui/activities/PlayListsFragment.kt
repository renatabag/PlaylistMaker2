package com.example.playlistmaker.presentation.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentListBinding
import com.example.playlistmaker.presentation.viewmodels.PlayListsViewModel
import org.koin.android.ext.android.inject
import kotlin.getValue


class PlayListsFragment : Fragment() {
    private val viewModel: PlayListsViewModel by inject()
    companion object {
        fun newInstance() = PlayListsFragment()
    }

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

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

        binding.emptyListState.text = getString(R.string.empty_playlist)
        binding.emptyMediaStateImage.setImageResource(R.drawable.empty)

        showEmptyState(true)
    }
    private fun showEmptyState(show: Boolean) {
        if (show) {
            binding.playlistsRecycler.visibility = View.GONE
            binding.emptyMediaStateImage.visibility = View.VISIBLE
            binding.emptyListState.visibility = View.VISIBLE
            binding.createPlaylistButton.visibility = View.VISIBLE
        } else {
            binding.playlistsRecycler.visibility = View.VISIBLE
            binding.emptyMediaStateImage.visibility = View.GONE
            binding.emptyListState.visibility = View.GONE
            binding.createPlaylistButton.visibility = View.GONE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}