package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var errorStateContainer: LinearLayout
    private lateinit var emptyImageView: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var errorImageView: ImageView
    private lateinit var connectionErrorMessage: TextView
    private lateinit var adapter: TrackAdapter
    private var searchText: String = ""

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isNetworkAvailable()) {
                connectionErrorMessage.text = "Интернет доступен"
            } else {
                showErrorState("Проблемы о связью")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcch)

        val backButton = findViewById<MaterialButton>(R.id.button_back)
        inputEditText = findViewById(R.id.inputEditText)
        recycler = findViewById(R.id.tracksList)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        errorStateContainer = findViewById(R.id.errorStateContainer)
        emptyImageView = findViewById(R.id.emptyImageView)
        placeholderMessage = findViewById(R.id.placeholderMessage)
        errorImageView = findViewById(R.id.errorImageView)
        connectionErrorMessage = findViewById(R.id.connectionErrorMessage)

        val retryButton = findViewById<Button>(R.id.retryButton)
        retryButton.setOnClickListener {
            performSearch()
        }

        adapter = TrackAdapter(emptyList())
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter

        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.VISIBLE

        backButton.setOnClickListener {
            finish()
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
            } else {
                inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.baseline_search_24), null,
                    ContextCompat.getDrawable(this, R.drawable.baseline_clear_24), null
                )
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
                performSearch()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        if (!isNetworkAvailable()) {
            showErrorState("Проблемы со связью")
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
        unregisterReceiver(networkChangeReceiver)
    }

    private fun performSearch() {
        if (!isNetworkAvailable()) {
            showErrorState("Проблемы со связью")
            return
        }

        val query = searchText.trim().lowercase(Locale.getDefault())

        if (query.isEmpty()) {
            restoreOriginalState()
            return
        }

        RetrofitClient.itunesApi.search(query).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results ?: emptyList()
                    adapter.updateTracks(tracks)

                    if (tracks.isEmpty()) {
                        showEmptyState()
                    } else {
                        showContent()
                    }
                } else {
                    showErrorState("Ошибка при загрузке данных: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                showErrorState("Ошибка сети: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    private fun restoreOriginalState() {
        adapter.updateTracks(emptyList())
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.VISIBLE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.VISIBLE
        errorStateContainer.visibility = View.GONE
    }

    private fun clearSearchInput() {
        inputEditText.setText("")
        hideKeyboard()
        restoreOriginalState()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(inputEditText.windowToken, 0)
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
    }

    private fun showErrorState(message: String) {
        recycler.visibility = View.GONE
        emptyStateContainer.visibility = View.GONE
        errorStateContainer.visibility = View.VISIBLE
        connectionErrorMessage.text = message
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    companion object {
        private const val SEARCH_TEXT = "SEARCH_TEXT"
    }
}