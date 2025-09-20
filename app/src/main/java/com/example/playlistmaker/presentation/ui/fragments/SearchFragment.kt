package com.example.playlistmaker.presentation.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearcchBinding
import com.example.playlistmaker.presentation.mappers.TrackUiMapper
import com.example.playlistmaker.presentation.ui.adapters.TrackAdapter
import com.example.playlistmaker.presentation.ui.states.SearchState
import com.example.playlistmaker.presentation.ui.states.TrackUi
import com.example.playlistmaker.presentation.viewmodels.SearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {
    private var _binding: ActivitySearcchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()
    private lateinit var adapter: TrackAdapter

    private var searchJob: Job? = null
    private val debounceDelay = 2000L

    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            activity?.runOnUiThread {
                binding.inputEditText.text?.toString()?.let { viewModel.searchTracks(it) }
            }
        }

        override fun onLost(network: Network) {
            activity?.runOnUiThread {
                showErrorState(
                    message = getString(R.string.network_error_message),
                    isNetworkError = true
                )
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            activity?.runOnUiThread {
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    binding.inputEditText.text?.toString()?.let { viewModel.searchTracks(it) }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySearcchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        initViews()
        setupAdapter()
        setupListeners()
        observeViewModel()
        checkNetworkState()
    }

    override fun onResume() {
        super.onResume()
        // Регистрируем NetworkCallback для отслеживания состояния сети
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onPause() {
        super.onPause()
        searchJob?.cancel() // Отменяем корутину при паузе
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun initViews() {
        try {
            val field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            field.set(binding.inputEditText, R.drawable.cursor_blue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupAdapter() {
        adapter = TrackAdapter(emptyList()) { trackUi ->
            val track = TrackUiMapper.mapToDomain(trackUi)
            viewModel.addTrackToHistory(track)

            val bundle = bundleOf("track" to trackUi)
            findNavController().navigate(R.id.track_player, bundle)
        }
        binding.tracksList.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksList.adapter = adapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {

        binding.clearIcon.setOnClickListener {
            clearSearchInput()
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }

        binding.inputEditText.doAfterTextChanged { text ->
            searchJob?.cancel() // Отменяем предыдущий поиск
            if (text.isNullOrEmpty()) {
                showSearchIcon()
                viewModel.searchTracks("")
            } else {
                showClearIcon()
                // Используем корутины вместо Handler для дебаунса
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(debounceDelay)
                    if (isActive) {
                        viewModel.searchTracks(text.toString())
                    }
                }
            }
        }

        binding.inputEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.inputEditText.right - binding.inputEditText.compoundPaddingEnd)) {
                    clearSearchInput()
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else false
        }
    }

    private fun showSearchIcon() {
        binding.inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_search_24),
            null, null, null
        )
    }

    private fun showClearIcon() {
        binding.inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_search_24),
            null,
            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_clear_24),
            null
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchState.collect { state ->
                    when (state) {
                        is SearchState.Loading -> {
                            if (!binding.inputEditText.text.isNullOrEmpty()) {
                                showLoading()
                            }
                        }
                        is SearchState.Content -> showContent(state.tracks)
                        SearchState.Empty -> showEmptyState()
                        is SearchState.History -> showHistory(state.tracks)
                        is SearchState.Error -> showErrorState(state.message, isNetworkError = true)
                        SearchState.EmptyHistory -> showEmptyHistoryState()
                        is SearchState.EmptyError -> showEmptyState()
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tracksList.visibility = View.GONE
        binding.emptyStateContainer.visibility = View.GONE
        binding.errorStateContainer.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
        binding.historyTitle.visibility = View.GONE
        binding.progressBar.bringToFront()
    }

    private fun showEmptyHistoryState() {
        binding.progressBar.visibility = View.GONE
        binding.tracksList.visibility = View.GONE
        binding.emptyStateContainer.visibility = View.GONE
        binding.errorStateContainer.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
        binding.historyTitle.visibility = View.GONE
        adapter.updateTracks(emptyList())
    }

    private fun showContent(tracks: List<TrackUi>) {
        adapter.updateTracks(tracks)
        binding.progressBar.visibility = View.GONE
        binding.tracksList.visibility = View.VISIBLE
        binding.emptyStateContainer.visibility = View.GONE
        binding.errorStateContainer.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
        binding.historyTitle.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.tracksList.visibility = View.GONE
        binding.emptyStateContainer.visibility = View.VISIBLE // Показываем пустое состояние
        binding.errorStateContainer.visibility = View.GONE
        binding.clearHistoryButton.visibility = View.GONE
        binding.historyTitle.visibility = View.GONE
    }

    private fun showHistory(tracks: List<TrackUi>) {
        if (tracks.isNotEmpty()) {
            adapter.updateTracks(tracks)
            binding.tracksList.visibility = View.VISIBLE
            binding.emptyStateContainer.visibility = View.GONE
            binding.errorStateContainer.visibility = View.GONE
            binding.clearHistoryButton.visibility = View.VISIBLE
            binding.historyTitle.visibility = View.VISIBLE
        } else {
            showEmptyHistoryState()
        }
    }

    private fun performSearch() {
        searchJob?.cancel() // Отменяем отложенный поиск
        binding.inputEditText.text?.toString()?.let { viewModel.searchTracks(it) }
        hideKeyboard()
    }

    private fun clearSearchInput() {
        binding.inputEditText.setText("")
        hideKeyboard()
        viewModel.clearSearch()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
    }

    private fun checkNetworkState() {
        if (!isNetworkAvailable()) {
            showErrorState(
                message = getString(R.string.network_error_message),
                isNetworkError = true
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun isNetworkAvailable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            @Suppress("DEPRECATION")
            return connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }

    private fun showErrorState(message: String, isNetworkError: Boolean) {
        binding.progressBar.visibility = View.GONE
        binding.tracksList.visibility = View.GONE
        binding.emptyStateContainer.visibility = View.GONE
        binding.errorStateContainer.visibility = View.VISIBLE

        binding.errorImageView.setImageResource(
            if (isNetworkError) R.drawable.error else R.drawable.empty
        )
        binding.connectionErrorMessage.text = message

        binding.retryButton.visibility = if (isNetworkError) View.VISIBLE else View.GONE
        binding.retryButton.setOnClickListener {
            if (isNetworkAvailable()) {
                performSearch()
            } else {
                showErrorState(getString(R.string.network_error_message), true)
            }
        }
    }

    companion object {
        fun newInstance() = SearchFragment()
    }
}