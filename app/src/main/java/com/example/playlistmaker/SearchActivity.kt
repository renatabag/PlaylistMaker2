package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
<<<<<<< HEAD
import android.text.Editable
import android.text.TextWatcher
=======
import android.os.Handler
import android.os.Looper
>>>>>>> d7cd583 (Commit 1)
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
<<<<<<< HEAD
import kotlin.random.Random
=======
import com.google.android.material.progressindicator.CircularProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
>>>>>>> d7cd583 (Commit 1)

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private var searchText: String = ""
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

<<<<<<< HEAD
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchText = s.toString()
=======
    private lateinit var progressBar: CircularProgressIndicator
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val debounceDelay = 2000L

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isNetworkAvailable()) {
                connectionErrorMessage.text = "Интернет доступен"
                performSearch()
            } else {
                showErrorState("Проблемы со связью", "Проверьте подключение к интернету")
            }
>>>>>>> d7cd583 (Commit 1)
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    @SuppressLint("ClickableViewAccessibility", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcch)

<<<<<<< HEAD
        val backButton = findViewById<MaterialButton>(R.id.button_back)
        inputEditText = findViewById(R.id.inputEditText)
=======
        inputEditText = findViewById(R.id.inputEditText)

        try {
            val field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            field.set(inputEditText, R.drawable.cursor_blue)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        searchHistory = SearchHistory(this)

        adapter = TrackAdapter(emptyList()) { track ->
            val intent = Intent(this, Track_player::class.java).apply {
                putExtra("TRACK", track)
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
>>>>>>> d7cd583 (Commit 1)

        backButton.setOnClickListener {
            finish()
        }

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_TEXT, "")
            inputEditText.setText(searchText)
        }

        inputEditText.addTextChangedListener(textWatcher)


        inputEditText.doAfterTextChanged { text ->
            if (text.isNullOrEmpty()) {
<<<<<<< HEAD
                inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.baseline_search_24), null, null, null);
            } else {
                inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.baseline_search_24), null, ContextCompat.getDrawable(this, R.drawable.baseline_clear_24), null);
=======
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
>>>>>>> d7cd583 (Commit 1)
            }
        }

        inputEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (inputEditText.right - inputEditText.compoundPaddingEnd)) {
                    inputEditText.setText("")
                    hideKeyboard()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

<<<<<<< HEAD

        val recycler = findViewById<RecyclerView>(R.id.tracksList)
        val tracks = listOf(
            Track(
                trackName = "Smells Like Teen Spirit",
                artistName = "Nirvana",
                trackTime = "5:01",
                artworkUrl100 = "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Billie Jean",
                artistName = "Michael Jackson",
                trackTime = "4:35",
                artworkUrl100 = "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Stayin' Alive",
                artistName = "Bee Gees",
                trackTime = "4:10",
                artworkUrl100 = "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Whole Lotta Love",
                artistName = "Led Zeppelin",
                trackTime = "5:33",
                artworkUrl100 = "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
            ),
            Track(
                trackName = "Sweet Child O'Mine",
                artistName = "Guns N' Roses",
                trackTime = "5:03",
                artworkUrl100 = "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
            )
        )
        recycler.layoutManager = LinearLayoutManager(this,  LinearLayoutManager.VERTICAL, false)
        recycler.adapter = TrackAdapter(tracks)
=======
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
            showErrorState("Нет подключения к интернету", "Проверьте подключение к интернету")
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
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun performSearch() {
        if (!isNetworkAvailable()) {
            showErrorState("Проблемы со связью", "Проверьте подключение к интернету")
            return
        }
        val query = searchText.trim().lowercase(Locale.getDefault())

        if (query.isEmpty()) {
            showHistory()
            return
        }

        showLoading()

        RetrofitClient.itunesApi.search(query).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                hideLoading()
                if (response.isSuccessful) {
                    val tracks = response.body()?.results ?: emptyList()
                    adapter.updateTracks(tracks)
                    if (tracks.isEmpty()) {
                        showEmptyState()
                    } else {
                        showContent()
                    }
                } else {
                    showErrorState("Ошибка при загрузке данных", "Код: ${response.code()}, Сообщение: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                showErrorState("Ошибка сети", t.message ?: "Неизвестная ошибка")
                t.printStackTrace()
            }
        })
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
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
>>>>>>> d7cd583 (Commit 1)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    companion object {
        private const val SEARCH_TEXT = "SEARCH_TEXT"
    }
}