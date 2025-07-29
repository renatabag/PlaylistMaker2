package com.example.playlistmaker.presentation.ui.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.presentation.ui.adapters.TrackAdapter
import com.example.playlistmaker.presentation.ui.states.SearchState
import com.example.playlistmaker.presentation.ui.states.TrackUi
import com.example.playlistmaker.presentation.viewmodels.SearchViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModels { Creator.provideSearchViewModelFactory() }

    private lateinit var inputEditText: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var errorStateContainer: LinearLayout
    private lateinit var placeholderMessage: TextView
    private lateinit var connectionErrorMessage: TextView
    private lateinit var adapter: TrackAdapter
    private lateinit var clearHistoryButton: Button
    private lateinit var historyTitle: TextView
    private lateinit var clearIcon: ImageView
    private lateinit var progressBar: CircularProgressIndicator

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val debounceDelay = 2000L

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isNetworkAvailable()) {
                connectionErrorMessage.text = "Интернет доступен"
                inputEditText.text?.toString()?.let { viewModel.searchTracks(it) }
            } else {
                showErrorState("Проблемы со связью")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(networkChangeReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(networkChangeReceiver, filter)
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SoonBlockedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcch)

        initViews()
        setupAdapter()
        setupListeners()
        observeViewModel()
        checkNetworkState()
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun initViews() {
        inputEditText = findViewById(R.id.inputEditText)
        recycler = findViewById(R.id.tracksList)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        errorStateContainer = findViewById(R.id.errorStateContainer)
        placeholderMessage = findViewById(R.id.placeholderMessage)
        connectionErrorMessage = findViewById(R.id.connectionErrorMessage)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        historyTitle = findViewById(R.id.historyTitle)
        clearIcon = findViewById(R.id.clearIcon)
        progressBar = findViewById(R.id.progressBar)

        try {
            val field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            field.set(inputEditText, R.drawable.cursor_blue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupAdapter() {
        adapter = TrackAdapter(emptyList()) { trackUi ->
            val track = TrackUi.toDomain(trackUi)
            viewModel.addTrackToHistory(track)
            val currentState = viewModel.searchState.value
            startActivity(TrackPlayer.getIntent(this, trackUi))

        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.button_back).setOnClickListener { finish() }
        clearIcon.setOnClickListener {
            clearSearchInput()
        }

        clearHistoryButton.setOnClickListener { viewModel.clearSearchHistory() }

        inputEditText.doAfterTextChanged { text ->
            if (text.isNullOrEmpty()) {
                showSearchIcon()
                viewModel.searchTracks("")
            } else {
                showClearIcon()
                scheduleSearch(text.toString())
            }
        }

        inputEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP &&
                event.rawX >= (inputEditText.right - inputEditText.compoundPaddingEnd)
            ) {
                clearSearchInput()
                true
            } else false
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else false
        }
    }

    private fun showSearchIcon() {
        inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(this, R.drawable.baseline_search_24),
            null, null, null
        )
    }

    private fun showClearIcon() {
        inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
            ContextCompat.getDrawable(this, R.drawable.baseline_search_24),
            null,
            ContextCompat.getDrawable(this, R.drawable.baseline_clear_24),
            null
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchState.collect { state ->
                    when (state) {
                        is SearchState.Loading -> {
                            if (!inputEditText.text.isNullOrEmpty()) {
                                showLoading()
                            }
                        }
                        is SearchState.Content -> showContent(state.tracks)
                        SearchState.Empty -> showEmptyState()

                        is SearchState.History -> {
                            showHistory(state.tracks)
                        }
                        is SearchState.Error -> showErrorState(state.message, isNetworkError = true)
                        is SearchState.EmptyError -> showErrorState(state.message, isNetworkError = false)
                        SearchState.EmptyHistory -> showEmptyHistoryState()
                    }
                }
            }
        }
    }

    private fun showCleanHistoryState() {
        progressBar.visibility = View.GONE
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        historyTitle.visibility = View.GONE
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        historyTitle.visibility = View.GONE
        progressBar.bringToFront()
    }
    private fun showEmptyHistoryState() {
        progressBar.visibility = View.GONE
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        historyTitle.visibility = View.GONE
        adapter.updateTracks(emptyList())
    }


    private fun showContent(tracks: List<TrackUi>) {
        adapter.updateTracks(tracks)
        progressBar.visibility = View.GONE
        recycler.visibility = View.VISIBLE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        historyTitle.visibility = View.GONE
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.VISIBLE
        errorStateContainer.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        historyTitle.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        progressBar.visibility = View.GONE
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.VISIBLE
        connectionErrorMessage.text = message
    }

    private fun showHistory(tracks: List<TrackUi>) {
        if (tracks.isNotEmpty()) {
            adapter.updateTracks(tracks) // Просто используем tracks как есть
            recycler.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
            errorStateContainer.visibility = View.GONE
            clearHistoryButton.visibility = View.VISIBLE
            historyTitle.visibility = View.VISIBLE
        } else {
            showCleanHistoryState()
        }
    }

    private fun scheduleSearch(query: String) {
        searchRunnable?.let { handler.removeCallbacks(it) }
        searchRunnable = Runnable { viewModel.searchTracks(query) }
        handler.postDelayed(searchRunnable!!, debounceDelay)
    }

    private fun performSearch() {
        searchRunnable?.let { handler.removeCallbacks(it) }
        inputEditText.text?.toString()?.let { viewModel.searchTracks(it) }
        hideKeyboard()
    }

    private fun clearSearchInput() {
        inputEditText.setText("")
        hideKeyboard()
        viewModel.clearSearch()
    }

    private fun hideKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun checkNetworkState() {
        if (!isNetworkAvailable()) {
            showErrorState("Нет подключения к интернету")
        }
    }

    override fun onPause() {
        super.onPause()
        searchRunnable?.let { handler.removeCallbacks(it) }
        unregisterReceiver(networkChangeReceiver)
    }
    private fun showErrorState(message: String, isNetworkError: Boolean) {
        progressBar.visibility = View.GONE
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.VISIBLE

        val errorImage = errorStateContainer.findViewById<ImageView>(R.id.errorImageView)
        val errorText = errorStateContainer.findViewById<TextView>(R.id.connectionErrorMessage)
        val retryButton = errorStateContainer.findViewById<Button>(R.id.retryButton)

        errorImage.setImageResource(
            if (isNetworkError) R.drawable.error else R.drawable.empty
        )
        errorText.text = message

        retryButton.visibility = if (isNetworkError) View.VISIBLE else View.GONE
        retryButton.setOnClickListener {
            inputEditText.text?.toString()?.let { query ->
                viewModel.searchTracks(query)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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

}