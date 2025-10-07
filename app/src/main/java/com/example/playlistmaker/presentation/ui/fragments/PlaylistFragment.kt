package com.example.playlistmaker.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.presentation.ui.adapters.TrackAdapter
import com.example.playlistmaker.presentation.ui.states.PlaylistState
import com.example.playlistmaker.presentation.ui.states.TrackUi
import com.example.playlistmaker.presentation.viewmodels.PlaylistViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class PlaylistFragment : Fragment() {

    private val viewModel: PlaylistViewModel by viewModel()
    private var _binding: FragmentPlaylistBinding? = null
    private val binding: FragmentPlaylistBinding get() = _binding!!
    private lateinit var tracksAdapter: TrackAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    companion object {
        private const val ARG_PLAYLIST_ID = "playlistId"

        fun newInstance(playlistId: Long): PlaylistFragment {
            val fragment = PlaylistFragment()
            val args = Bundle()
            args.putLong(ARG_PLAYLIST_ID, playlistId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistId = arguments?.getLong(ARG_PLAYLIST_ID) ?: -1L
        if (playlistId == -1L) {
            findNavController().navigateUp()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        setupBottomSheet()
        setupObservers()

        viewModel.loadPlaylist(playlistId)
    }

    override fun onResume() {
        super.onResume()
        // Гарантируем скрытие навигации при каждом показе фрагмента
        hideBottomNavigation()
    }

    override fun onPause() {
        super.onPause()
        // Показываем навигацию только когда полностью уходим из фрагмента
        showBottomNavigation()
    }

    private fun hideBottomNavigation() {
        try {
            val activity = requireActivity()
            val bottomNav = activity.findViewById<View>(R.id.bottom_navigation)
            bottomNav?.visibility = View.GONE
            println("Bottom navigation HIDDEN in PlaylistFragment")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showBottomNavigation() {
        try {
            val activity = requireActivity()
            val bottomNav = activity.findViewById<View>(R.id.bottom_navigation)
            bottomNav?.visibility = View.VISIBLE
            println("Bottom navigation SHOWN in PlaylistFragment")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.tracksBottomSheet)

        bottomSheetBehavior.apply {
            isHideable = false
            isDraggable = true
            skipCollapsed = false
            isFitToContents = false

            view?.post {
                val peekHeight = calculateInitialPeekHeight()
                println("Setting peek height: $peekHeight")
                setPeekHeight(peekHeight, true)
                state = BottomSheetBehavior.STATE_COLLAPSED
            }

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    println("Bottom Sheet state: $newState")
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }
    }

    private fun playTrack(track: TrackUi) {
        val trackPlayerFragment = TrackPlayerFragment.newInstance(track)

        parentFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, trackPlayerFragment)
            .addToBackStack("playlist_to_player")
            .commit()

        println("Воспроизведение: ${track.trackName} - ${track.artistName}")
    }

    private fun calculateInitialPeekHeight(): Int {
        val buttonsContainer = binding.buttonsContainer

        println("Buttons container: $buttonsContainer")
        println("Buttons container is laid out: ${buttonsContainer?.isLaidOut}")

        if (buttonsContainer != null && buttonsContainer.isLaidOut) {
            val location = IntArray(2)
            buttonsContainer.getLocationOnScreen(location)
            val bottomElementBottom = location[0] + buttonsContainer.height + 174
            val margin24dp = (24 * resources.displayMetrics.density).toInt()
            val calculatedHeight = bottomElementBottom + margin24dp
            println("Calculated peek height: $calculatedHeight")
            return calculatedHeight
        } else {
            val defaultHeight = calculateDefaultHeight()
            println("Using default height: $defaultHeight")
            return defaultHeight
        }
    }

    private fun calculateDefaultHeight(): Int {
        val displayMetrics = resources.displayMetrics
        val contentHeight = (200 + 120 + 50 + 50 + 50) * displayMetrics.density
        val margin24dp = (24 * displayMetrics.density).toInt()
        return contentHeight.toInt() + margin24dp
    }

    private fun setupRecyclerView() {
        tracksAdapter = TrackAdapter(
            tracks = emptyList(),
            onTrackClick = { track ->
                playTrack(track)
            }
        )

        binding.tracksRecyclerView.apply {
            adapter = tracksAdapter
            layoutManager = LinearLayoutManager(requireContext())
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistState.collectLatest { state ->
                when (state) {
                    is PlaylistState.Loading -> showLoading()
                    is PlaylistState.Empty -> showEmptyState() // Добавьте этот case
                    is PlaylistState.Content -> showPlaylist(state)
                    is PlaylistState.Error -> showError(state.message)
                }
            }
        }
    }
    private fun showEmptyState() {
        // Скрываем Bottom Sheet
        binding.tracksBottomSheet.visibility = View.GONE

        // Показываем Toast
        showEmptyPlaylistToast()

        // Обновляем информацию о плейлисте (название, описание)
        // Для времени и количества треков показываем "0"
        binding.allTime.text = "0 мин"
        binding.tracksCount.text = "0 треков"
    }
    private fun showEmptyPlaylistToast() {
        Toast.makeText(
            requireContext(),
            "В этом плейлисте пока нет треков",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupClickListeners() {
        binding.menuButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.shareButton.setOnClickListener {
            sharePlaylist()
        }

        binding.settingsButton.setOnClickListener {
            showPlaylistSettings()
        }
    }

    private fun showLoading() {
        // Можно показать ProgressBar
    }

    private fun showPlaylist(state: PlaylistState.Content) {
        binding.tracksBottomSheet.visibility = View.VISIBLE

        // Заполняем данные плейлиста
        binding.textView1.text = state.playlist.name
        binding.textView2.text = state.playlist.description ?: ""

        val trackCount = state.playlist.trackCount
        binding.tracksCount.text = formatTrackCount(trackCount)

        val totalTimeMs = state.tracks.sumOf { it.trackTimeMillis }
        val totalTimeFormatted = formatTotalTime(totalTimeMs)
        binding.allTime.text = totalTimeFormatted

        tracksAdapter.updateTracks(state.tracks)

        view?.postDelayed({
            val newPeekHeight = calculateInitialPeekHeight()
            bottomSheetBehavior.setPeekHeight(newPeekHeight, true)
        }, 200)
    }

    private fun formatTrackCount(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "$count трек"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
            else -> "$count треков"
        }
    }

    private fun formatTotalTime(totalTimeMs: Long): String {
        val totalSeconds = totalTimeMs / 1000
        val minutes = (totalSeconds % 3600) / 60
        return String.format(Locale.getDefault(), "%d мин", minutes)
    }

    private fun sharePlaylist() {
        // TODO: Реализация шаринга плейлиста
    }

    private fun showPlaylistSettings() {
        // TODO: Реализация показа настроек плейлиста
    }

    private fun showError(message: String) {
        println("Ошибка загрузки плейлиста: $message")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}