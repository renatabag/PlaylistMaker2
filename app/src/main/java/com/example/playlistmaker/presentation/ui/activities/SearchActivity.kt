package com.example.playlistmaker.presentation.ui.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.data.SearchHistory
import com.example.playlistmaker.data.dto.TrackResponseDto
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.RetrofitClient
import com.example.playlistmaker.presentation.ui.adapters.TrackAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class SearchActivity : AppCompatActivity() {
    private lateinit var inputEditText: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var errorStateContainer: LinearLayout
    private lateinit var placeholderMessage: TextView
    private lateinit var connectionErrorMessage: TextView
    private lateinit var adapter: TrackAdapter
    private var searchText: String = ""
    private lateinit var searchHistory: SearchHistory
    private lateinit var clearHistoryButton: Button
    private lateinit var historyTitle: TextView
    private lateinit var clearIcon: ImageView

    private lateinit var progressBar: CircularProgressIndicator
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val debounceDelay = 2000L
    private val trackMapper = TrackMapper()

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isNetworkAvailable()) {
                connectionErrorMessage.text = "Интернет доступен"
                performSearch()
            } else {
                showErrorState("Проблемы со связью", "Загрузка не удалась. Проверьте подключение к интернету")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "WrongViewCast", "SoonBlockedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcch)

        inputEditText = findViewById(R.id.inputEditText)

        try {
            val field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            field.set(inputEditText, R.drawable.cursor_blue)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        searchHistory = SearchHistory(this, trackMapper)

        adapter = TrackAdapter(emptyList()) { track ->
            val intent = Intent(this, TrackPlayer::class.java).apply {
                putExtra("TRACK", track) // Теперь передаем Parcelable объект
            }
            startActivity(intent)
            searchHistory.addTrack(track)
        }

        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        clearHistoryButton.setOnClickListener {
            clearHistory()
        }

        historyTitle = findViewById(R.id.historyTitle)
        clearIcon = findViewById(R.id.clearIcon)

        progressBar = findViewById(R.id.progressBar)
        val backButton = findViewById<MaterialButton>(R.id.button_back)
        inputEditText = findViewById(R.id.inputEditText)
        recycler = findViewById(R.id.tracksList)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        errorStateContainer = findViewById(R.id.errorStateContainer)
        placeholderMessage = findViewById(R.id.placeholderMessage)
        connectionErrorMessage = findViewById(R.id.connectionErrorMessage)
        val retryButton = findViewById<Button>(R.id.retryButton)
        retryButton.setOnClickListener {
            performSearch()
        }

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter

        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.VISIBLE

        backButton.setOnClickListener {
            finish()
        }

        clearIcon.setOnClickListener {
            clearSearchInput()
        }

        if (inputEditText.text.isNullOrEmpty()) {
            showHistory()
        }

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_TEXT, "")
            inputEditText.setText(searchText)
        }

        inputEditText.doAfterTextChanged { text ->
            searchText = text?.toString() ?: ""
            if (text.isNullOrEmpty()) {
                inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.baseline_search_24), null, null, null
                )
                showHistory()
            } else {
                inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.baseline_search_24), null,
                    ContextCompat.getDrawable(this, R.drawable.baseline_clear_24), null
                )
                searchRunnable?.let { handler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    performSearch()
                }
                handler.postDelayed(searchRunnable!!, debounceDelay)
            }
        }

        inputEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (inputEditText.right - inputEditText.compoundPaddingEnd)) {
                    clearSearchInput()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRunnable?.let { handler.removeCallbacks(it) }
                performSearch()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        if (!isNetworkAvailable()) {
            historyTitle.visibility = View.GONE
            showErrorState("Нет подключения к интернету", "Загрузка не удалась. Проверьте подключение к интернету")
        } else {
            showContent()
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onPause() {
        super.onPause()
        searchRunnable?.let { handler.removeCallbacks(it) }
        unregisterReceiver(networkChangeReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun showHistory() {
        val history = searchHistory.getHistory()
        if (history.isNotEmpty()) {
            adapter.updateTracks(history)
            recycler.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
            errorStateContainer.visibility = View.GONE
            clearHistoryButton.visibility = View.VISIBLE
            historyTitle.visibility = View.VISIBLE
        } else {
            recycler.visibility = View.GONE
            emptyStateContainer.visibility = View.GONE
            errorStateContainer.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
            historyTitle.visibility = View.GONE
        }
    }


    private fun clearSearchInput() {
        inputEditText.setText("")
        hideKeyboard()
        showHistory()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun performSearch() {
        if (!isNetworkAvailable()) {
            showErrorState("Проблемы со связью", "Загрузка не удалась. Проверьте подключение к интернету")
            return
        }

        val query = searchText.trim().lowercase(Locale.getDefault())
        if (query.isEmpty()) {
            showHistory()
            return
        }

        showLoading()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.itunesApi.search(query) // Response<TrackResponseDto>
                if (response.isSuccessful) {
                    val trackResponseDto = response.body()
                    val tracksDto = trackResponseDto?.tracks ?: emptyList()  // <-- Здесь tracks, а не results
                    val tracks = tracksDto.map { trackMapper.mapToDomain(it) }
                    hideLoading()

                    if (tracks.isEmpty()) {
                        showEmptyState()
                    } else {
                        adapter.updateTracks(tracks)
                        showContent()
                    }
                } else {
                    hideLoading()
                    showErrorState("Ошибка сети", "Код ошибки: ${response.code()}")
                }
            } catch (e: Exception) {
                hideLoading()
                showErrorState("Ошибка сети", e.message ?: "Неизвестная ошибка")
                e.printStackTrace()
            }
        }
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

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showContent() {
        recycler.visibility = View.VISIBLE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.GONE
    }

    private fun showEmptyState() {
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.VISIBLE
        errorStateContainer.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        historyTitle.visibility = View.GONE
    }

    private fun showErrorState(title: String, message: String) {
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.VISIBLE
        connectionErrorMessage.text = "$title\n$message"
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    private fun clearHistory() {
        searchHistory.clearHistory()
        showHistory()
    }

    companion object {
        private const val SEARCH_TEXT = "SEARCH_TEXT"
    }
}