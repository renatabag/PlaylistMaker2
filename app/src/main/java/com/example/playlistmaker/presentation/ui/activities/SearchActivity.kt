package com.example.playlistmaker.presentation.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.App
import com.example.playlistmaker.presentation.ui.adapters.TrackAdapter
import com.example.playlistmaker.presentation.ui.states.SearchState
import com.example.playlistmaker.presentation.viewmodels.ParcelableTrack
import com.example.playlistmaker.presentation.viewmodels.SearchViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var viewModel: SearchViewModel
    private var isSearchPerformed = false // Флаг выполнения поиска (вынесен на уровень класса)

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
    private val debounceDelay = 1000L

    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread {
                connectionErrorMessage.text = "Интернет доступен"
                val query = inputEditText.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.performSearch(query)
                }
            }
        }

        override fun onLost(network: Network) {
            runOnUiThread {
                showErrorState("Проблемы со связью", "Проверьте подключение к интернету")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcch)

        val app = application as App
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel(app.searchInteractor) as T
                }
            }
        )[SearchViewModel::class.java]

        initViews()
        setupAdapter()
        observeViewModel()
        setupListeners()
        registerNetworkCallback()

        if (!isNetworkAvailable()) {
            showErrorState("Нет подключения", "Проверьте интернет")
        } else {
            viewModel.getHistory()
        }
    }

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
    }

    private fun setupAdapter() {
        adapter = TrackAdapter(emptyList()) { parcelableTrack ->
            startActivity(Intent(this, TrackPlayer::class.java).apply {
                putExtra("TRACK", parcelableTrack)
            })
            viewModel.addToHistory(parcelableTrack.track)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchState.collect { state ->
                    when (state) {
                        is SearchState.Content -> showContent(state.tracks)
                        is SearchState.Empty -> {
                            if (isSearchPerformed) {
                                showEmptyState()
                            } else {
                                showInitialState()
                            }
                        }
                        is SearchState.Error -> showErrorState("Ошибка", state.message)
                        is SearchState.History -> showHistory(state.tracks)
                        is SearchState.Initial -> showInitialState() // Обработка начального состояния
                        SearchState.Loading -> showLoading()
                    }
                }
            }
        }
    }

    private fun showInitialState() {
        // Скрываем все элементы - пустой экран
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        historyTitle.visibility = View.GONE
    }

    private fun setupListeners() {
        findViewById<MaterialButton>(R.id.button_back).setOnClickListener { finish() }

        clearIcon.setOnClickListener {
            clearSearchInput()
        }

        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        inputEditText.doAfterTextChanged { text ->
            searchRunnable?.let(handler::removeCallbacks)
            searchRunnable = Runnable {
                isSearchPerformed = true // Помечаем, что поиск выполнен
                viewModel.performSearch(text?.toString().orEmpty())
            }
            handler.postDelayed(searchRunnable!!, debounceDelay)
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRunnable?.let(handler::removeCallbacks)
                isSearchPerformed = true // Помечаем, что поиск выполнен
                viewModel.performSearch(inputEditText.text.toString())
                hideKeyboard()
                true
            } else false
        }
    }

    private fun showHistory(tracks: List<ParcelableTrack>) {
        adapter.updateTracks(tracks)
        toggleViews(showRecycler = tracks.isNotEmpty(), showHistory = true)
    }

    private fun showContent(tracks: List<ParcelableTrack>) {
        adapter.updateTracks(tracks)
        toggleViews(showRecycler = true)
    }

    private fun showLoading() {
        toggleViews(showProgress = true)
    }

    private fun showEmptyState() {
        toggleViews(showEmpty = true)
    }

    @SuppressLint("SetTextI18n")
    private fun showErrorState(title: String, message: String) {
        connectionErrorMessage.text = "$title\n$message"
        toggleViews(showError = true)
    }

    private fun toggleViews(
        showRecycler: Boolean = false,
        showEmpty: Boolean = false,
        showError: Boolean = false,
        showProgress: Boolean = false,
        showHistory: Boolean = false
    ) {
        recycler.visibility = if (showRecycler) View.VISIBLE else View.GONE
        emptyStateContainer.visibility = if (showEmpty) View.VISIBLE else View.GONE
        errorStateContainer.visibility = if (showError) View.VISIBLE else View.GONE
        progressBar.visibility = if (showProgress) View.VISIBLE else View.GONE
        clearHistoryButton.visibility = if (showHistory) View.VISIBLE else View.GONE
        historyTitle.visibility = if (showHistory) View.VISIBLE else View.GONE
    }

    private fun clearSearchInput() {
        inputEditText.text.clear()
        hideKeyboard()
        isSearchPerformed = false // Сбрасываем флаг при очистке
        viewModel.getHistory() // Возвращаемся к истории/начальному состоянию
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun registerNetworkCallback() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    private fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchRunnable?.let(handler::removeCallbacks)
        unregisterNetworkCallback()
    }
}