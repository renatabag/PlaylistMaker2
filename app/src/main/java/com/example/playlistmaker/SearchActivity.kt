package com.example.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var retryButton: Button
    private lateinit var tracksList: RecyclerView
    private lateinit var clearIcon: ImageView
    private var emptyView: View? = null
    private var errorView: View? = null
    private val trackAdapter = TrackAdapter(emptyList())

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcch)

        inputEditText = findViewById(R.id.inputEditText)
        retryButton = findViewById(R.id.retryButton)
        tracksList = findViewById(R.id.tracksList)
        clearIcon = findViewById(R.id.clearIcon)

        tracksList.layoutManager = LinearLayoutManager(this)
        tracksList.adapter = trackAdapter

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearIcon.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {
                performSearch(s.toString())
            }
        })

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(inputEditText.text.toString())
                true
            } else {
                false
            }
        }

        retryButton.setOnClickListener {
            performSearch(inputEditText.text.toString())
        }

        clearIcon.setOnClickListener {
            inputEditText.text.clear()
            tracksList.visibility = View.GONE
            emptyView?.visibility = View.GONE
            errorView?.visibility = View.GONE
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            RetrofitClient.instance.search(query).enqueue(object : Callback<TrackResponse> {
                override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                    if (response.isSuccessful) {
                        val tracks = response.body()?.results ?: emptyList()
                        if (tracks.isEmpty()) {
                            showEmptyState()
                        } else {
                            showTracks(tracks)
                        }
                    } else {
                        showErrorState()
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    showErrorState()
                }
            })
        }
    }

    private fun showTracks(tracks: List<Track>) {
        tracksList.visibility = View.VISIBLE
        retryButton.visibility = View.GONE
        emptyView?.visibility = View.GONE
        errorView?.visibility = View.GONE
        trackAdapter.updateTracks(tracks)
    }

    private fun showEmptyState() {
        if (emptyView == null) {
            emptyView = this.findViewById(R.id.emptyState)
        }
        tracksList.visibility = View.GONE
        retryButton.visibility = View.GONE
        emptyView?.visibility = View.VISIBLE
        errorView?.visibility = View.GONE
    }

    private fun showErrorState() {
        if (errorView == null) {
            errorView = findViewById(R.id.errorState)
            errorView?.findViewById<Button>(R.id.retryButton)?.setOnClickListener {
                performSearch(inputEditText.text.toString())
            }
        }
        tracksList.visibility = View.GONE
        retryButton.visibility = View.VISIBLE
        emptyView?.visibility = View.GONE
        errorView?.visibility = View.VISIBLE
    }
}