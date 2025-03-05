package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var placeholderMessage: TextView
    private lateinit var connectionErrorMessage: TextView
    private lateinit var emptyImageView: ImageView
    private var searchText: String = ""

    private val originalTracks = listOf(
        Track(
            trackName = "Smells Like Teen Spirit",
            artistName = "Nirvana",
            trackTime = Track.formatTrackTime(301000L),
            artworkUrl100 = "https://example.com/nirvana.jpg"
        ),
        Track(
            trackName = "Billie Jean",
            artistName = "Michael Jackson",
            trackTime = Track.formatTrackTime(275000L),
            artworkUrl100 = "https://example.com/mj.jpg"
        ),
        Track(
            trackName = "Stayin' Alive",
            artistName = "Bee Gees",
            trackTime = Track.formatTrackTime(250000L),
            artworkUrl100 = "https://example.com/beegees.jpg"
        ),
        Track(
            trackName = "Whole Lotta Love",
            artistName = "Led Zeppelin",
            trackTime = Track.formatTrackTime(333000L),
            artworkUrl100 = "https://example.com/ledzeppelin.jpg"
        ),
        Track(
            trackName = "Sweet Child O'Mine",
            artistName = "Guns N' Roses",
            trackTime = Track.formatTrackTime(303000L),
            artworkUrl100 = "https://example.com/gunsnroses.jpg"
        )
    )

    @SuppressLint("ClickableViewAccessibility", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcch)

        val backButton = findViewById<MaterialButton>(R.id.button_back)
        inputEditText = findViewById(R.id.inputEditText)
        recycler = findViewById(R.id.tracksList)
        placeholderMessage = findViewById(R.id.placeholderMessage)
        connectionErrorMessage = findViewById(R.id.connectionErrorMessage)
        emptyImageView = findViewById(R.id.emptyImageView)

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
                    clearSearchInput() // Очищаем поле ввода и восстанавливаем список
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch() // Выполнение поискового запроса
                hideKeyboard() // Скрытие клавиатуры
                true
            } else {
                false
            }
        }

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = TrackAdapter(originalTracks) // Изначально показываем все треки

        // Управление видимостью RecyclerView и TextView при старте
        if (originalTracks.isEmpty()) {
            recycler.visibility = View.GONE
            placeholderMessage.visibility = View.VISIBLE
            connectionErrorMessage.visibility = View.GONE
        } else {
            recycler.visibility = View.VISIBLE
            placeholderMessage.visibility = View.GONE
            connectionErrorMessage.visibility = View.GONE
        }
    }

    private fun performSearch() {
        val query = searchText.trim().lowercase(Locale.getDefault())
        val filteredTracks = if (query.isEmpty()) {
            originalTracks // Если запрос пустой, показываем все треки
        } else {
            originalTracks.filter { track ->
                track.trackName.lowercase(Locale.getDefault()).contains(query) ||
                        track.artistName.lowercase(Locale.getDefault()).contains(query)
            }
        }

        // Обновляем адаптер RecyclerView
        recycler.adapter = TrackAdapter(filteredTracks)

        // Управление видимостью RecyclerView, картинки и текста
        if (filteredTracks.isEmpty()) {
            recycler.visibility = View.GONE
            emptyImageView.visibility = View.VISIBLE
            placeholderMessage.visibility = View.VISIBLE
            connectionErrorMessage.visibility = View.GONE
        } else {
            recycler.visibility = View.VISIBLE
            emptyImageView.visibility = View.GONE
            placeholderMessage.visibility = View.GONE
            connectionErrorMessage.visibility = View.GONE
        }
    }

    private fun clearSearchInput() {
        inputEditText.setText("") // Очищаем поле ввода
        hideKeyboard() // Скрываем клавиатуру
        restoreOriginalTracks() // Восстанавливаем оригинальный список треков
    }

    private fun restoreOriginalTracks() {
        recycler.adapter = TrackAdapter(originalTracks) // Обновляем адаптер с оригинальным списком

        // Управление видимостью RecyclerView, картинки и текста
        if (originalTracks.isEmpty()) {
            recycler.visibility = View.GONE
            emptyImageView.visibility = View.VISIBLE
            placeholderMessage.visibility = View.VISIBLE
            connectionErrorMessage.visibility = View.GONE
        } else {
            recycler.visibility = View.VISIBLE
            emptyImageView.visibility = View.GONE
            placeholderMessage.visibility = View.GONE
            connectionErrorMessage.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    private fun showMessage(text: String, additionalMessage: String, isConnectionError: Boolean = false) {
        if (text.isNotEmpty()) {
            if (isConnectionError) {
                // Показываем сообщение о проблемах со связью
                connectionErrorMessage.visibility = View.VISIBLE
                connectionErrorMessage.text = text
                placeholderMessage.visibility = View.GONE // Скрываем обычное сообщение
                emptyImageView.visibility = View.GONE // Скрываем картинку
            } else {
                // Показываем обычное сообщение
                placeholderMessage.visibility = View.VISIBLE
                placeholderMessage.text = text
                connectionErrorMessage.visibility = View.GONE // Скрываем сообщение о проблемах со связью
                emptyImageView.visibility = View.VISIBLE // Показываем картинку
            }

            // Очищаем список и обновляем адаптер
            recycler.adapter = TrackAdapter(emptyList())

            // Показываем дополнительное сообщение в Toast
            if (additionalMessage.isNotEmpty()) {
                Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG).show()
            }
        } else {
            // Скрываем оба сообщения, если текст пуст
            placeholderMessage.visibility = View.GONE
            connectionErrorMessage.visibility = View.GONE
            emptyImageView.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
    }

    companion object {
        private const val SEARCH_TEXT = "SEARCH_TEXT"
    }
}