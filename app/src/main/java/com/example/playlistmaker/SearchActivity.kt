package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

interface OnItemClickListener {
    fun onItemClick(track: Track)
}

class SearchActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    lateinit var adapter: TrackAdapter
    private val tracks: MutableList<Track> = mutableListOf()

    private lateinit var inputEditText: EditText
    private var searchText: String = ""
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchText = s.toString()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    @SuppressLint("ClickableViewAccessibility", "WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcch)

        recyclerView = findViewById(R.id.recyclerView) // <-- Corrected ID
        recyclerView.layoutManager = LinearLayoutManager(this)

        val onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(track: Track) {
                // Handle click (e.g., open a new screen with track information)
                println("Clicked on track: ${track.trackName}") // Example
            }
        }

        adapter = TrackAdapter(tracks, onItemClickListener)
        recyclerView.adapter = adapter

        val backButton = findViewById<MaterialButton>(R.id.button_back)
        inputEditText = findViewById(R.id.inputEditText)

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
                inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.baseline_search_24), null, null, null
                )
            } else {
                inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.baseline_search_24),
                    null,
                    ContextCompat.getDrawable(this, R.drawable.baseline_clear_24),
                    null
                )
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    companion object {
        private const val SEARCH_TEXT = "SEARCH_TEXT"
    }
}