package com.example.playlistmaker.presentation.ui.fragments

import android.content.Intent
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

    private var currentPlaylistId: Long = -1L
    private var currentPlaylistName: String = ""
    private var currentPlaylistDescription: String = ""
    private var currentTracks: List<TrackUi> = emptyList()

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
            },
            onTrackLongClick = { track ->  // Добавьте этот параметр
                showDeleteTrackDialog(track)
            }
        )

        binding.tracksRecyclerView.apply {
            adapter = tracksAdapter
            layoutManager = LinearLayoutManager(requireContext())
            overScrollMode = View.OVER_SCROLL_NEVER
        }
    }
    private fun showDeleteTrackDialog(track: TrackUi) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удаление трека")
            .setMessage("Хотите удалить трек ?")
            .setNegativeButton("НЕТ") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("ДА") { dialog, _ ->
                deleteTrackFromPlaylist(track)
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
    private fun deleteTrackFromPlaylist(track: TrackUi) {
        val playlistId = arguments?.getLong(ARG_PLAYLIST_ID) ?: -1L
        if (playlistId != -1L) {
            println("Начало удаления трека: ${track.trackName} (ID: ${track.trackId}) из плейлиста $playlistId") // Отладка

            // Мгновенно удаляем трек из адаптера
            val currentTracks = tracksAdapter.getCurrentTracks().toMutableList()
            val initialCount = currentTracks.size
            currentTracks.remove(track)
            tracksAdapter.updateTracks(currentTracks)

            println("Трек удален из UI: было $initialCount, стало ${currentTracks.size}") // Отладка

            // Обновляем информацию о плейлисте
            updatePlaylistInfoAfterDeletion(currentTracks)

            // Вызываем ViewModel для удаления из базы данных
            viewModel.deleteTrackFromPlaylist(playlistId, track.trackId.toLong())

            // Показываем Toast с подтверждением
            Toast.makeText(
                requireContext(),
                "Трек \"${track.trackName}\" удален из плейлиста",
                Toast.LENGTH_SHORT
            ).show()

            println("Запрос на удаление из БД отправлен в ViewModel") // Отладка
        } else {
            println("Ошибка: playlistId не найден") // Отладка
        }
    }


    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistState.collectLatest { state ->
                when (state) {
                    is PlaylistState.Loading -> showLoading()
                    is PlaylistState.Empty -> showEmptyState(state)
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


    private fun showError(message: String) {
        println("Ошибка загрузки плейлиста: $message")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun sharePlaylist() {
        if (currentTracks.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "В этом плейлисте нет списка треков, которым можно поделиться",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val shareText = buildShareText()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareChooser = Intent.createChooser(shareIntent, "Поделиться плейлистом")
        startActivity(shareChooser)
    }

    private fun buildShareText(): String {
        val stringBuilder = StringBuilder()

        // Название плейлиста
        stringBuilder.append(currentPlaylistName)
        stringBuilder.append("\n")

        // Описание (если есть)
        if (currentPlaylistDescription.isNotEmpty()) {
            stringBuilder.append(currentPlaylistDescription)
            stringBuilder.append("\n")
        }

        // Количество треков
        val trackCount = currentTracks.size
        stringBuilder.append("$trackCount треков")
        stringBuilder.append("\n\n")

        // Список треков
        currentTracks.forEachIndexed { index, track ->
            val trackNumber = index + 1
            val trackTimeFormatted = formatTrackTimeForShare(track.trackTimeMillis)
            stringBuilder.append("$trackNumber. ${track.artistName} - ${track.trackName} ($trackTimeFormatted)")
            if (index < currentTracks.size - 1) {
                stringBuilder.append("\n")
            }
        }

        return stringBuilder.toString()
    }

    private fun formatTrackTimeForShare(trackTimeMillis: Long): String {
        val totalSeconds = trackTimeMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
    private fun showPlaylistSettings() {
        val bottomSheet = PlaylistSettingsBottomSheet.newInstance(
            playlistId = currentPlaylistId,
            playlistName = currentPlaylistName,
            trackCount = currentTracks.size,
            description = currentPlaylistDescription
        )

        bottomSheet.setListener(object : PlaylistSettingsListener {
            override fun onSharePlaylist() {
                sharePlaylist()
            }

            override fun onEditPlaylist() {
                // TODO: Реализовать редактирование плейлиста
                Toast.makeText(requireContext(), "Редактирование плейлиста: $currentPlaylistName", Toast.LENGTH_SHORT).show()
            }

            override fun onDeletePlaylist() {
                deleteCurrentPlaylist()
            }
        })

        bottomSheet.show(parentFragmentManager, PlaylistSettingsBottomSheet.TAG)
    }

    private fun deleteCurrentPlaylist() {
        val playlistName = currentPlaylistName

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удаление плейлиста")
            .setMessage("Плейлист \"$playlistName\" будет удален")
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Удалить") { dialog, _ ->
                // Вызываем ViewModel для удаления плейлиста
                viewModel.deletePlaylist(currentPlaylistId)

                // Показываем Toast
                Toast.makeText(
                    requireContext(),
                    "Плейлист \"$playlistName\" удален",
                    Toast.LENGTH_SHORT
                ).show()

                // Безопасное возвращение на экран списка плейлистов
                safelyNavigateBackToPlaylists()
            }
            .create()
            .show()
    }
    private fun showPlaylist(state: PlaylistState.Content) {
        // Показываем bottom sheet и скрываем empty state
        binding.tracksBottomSheet.visibility = View.VISIBLE
        binding.emptyStateContainer.visibility = View.GONE

        // Сохраняем актуальные данные плейлиста
        currentPlaylistId = state.playlist.id
        currentPlaylistName = state.playlist.name
        currentPlaylistDescription = state.playlist.description ?: ""
        currentTracks = state.tracks

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

    private fun updatePlaylistInfoAfterDeletion(updatedTracks: List<TrackUi>) {
        val trackCount = updatedTracks.size
        binding.tracksCount.text = formatTrackCount(trackCount)

        val totalTimeMs = updatedTracks.sumOf { it.trackTimeMillis }
        val totalTimeFormatted = formatTotalTime(totalTimeMs)
        binding.allTime.text = totalTimeFormatted

        // Если треков не осталось, переключаем на empty state
        if (updatedTracks.isEmpty()) {
            binding.emptyStateContainer.visibility = View.GONE
            binding.tracksBottomSheet.visibility = View.GONE
            showEmptyPlaylistToast()
        }
    }
    private fun showEmptyState(state: PlaylistState.Empty) {
        // Скрываем Bottom Sheet
        binding.tracksBottomSheet.visibility = View.GONE

        // Заполняем данные плейлиста из состояния
        binding.textView1.text = state.playlist.name
        binding.textView2.text = state.playlist.description ?: ""

        // Для времени и количества треков показываем "0"
        binding.allTime.text = "0 мин"
        binding.tracksCount.text = "0 треков"

        // Показываем Toast
        showEmptyPlaylistToast()
    }

    private fun safelyNavigateBackToPlaylists() {
        try {
            // Способ 1: Попробовать найти фрагмент списка плейлистов в back stack
            val fragmentManager = parentFragmentManager
            val backStackEntryCount = fragmentManager.backStackEntryCount

            // Ищем запись в back stack, которая соответствует списку плейлистов
            for (i in backStackEntryCount - 1 downTo 0) {
                val backStackEntry = fragmentManager.getBackStackEntryAt(i)
                if (backStackEntry.name == "playlist_details" || backStackEntry.name?.contains("playlist") == true) {
                    // Нашли запись - возвращаемся к ней
                    fragmentManager.popBackStack(backStackEntry.id, 0)
                    return
                }
            }

            // Способ 2: Если не нашли в back stack, используем navigateUp
            if (findNavController().currentDestination?.id == R.id.playlistsFragment) {
                findNavController().navigateUp()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Способ 3: Аварийный возврат на главный экран
            try {
                findNavController().popBackStack(R.id.playlistsFragment, false)
            } catch (e2: Exception) {
                // Последняя попытка - просто назад
                requireActivity().onBackPressed()
            }
        }
    }


}