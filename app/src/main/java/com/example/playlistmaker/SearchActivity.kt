package com.example.playlistmaker
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.playlistmaker.R
import com.google.android.material.button.MaterialButton

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText

    @SuppressLint("ClickableViewAccessibility", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcch)

        val backButton = findViewById<MaterialButton>(R.id.button_back)
        inputEditText = findViewById(R.id.inputEditText)

        backButton.setOnClickListener {
            finish()
        }

        inputEditText.doAfterTextChanged { text ->
            if (text.isNullOrEmpty()) {
                inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.baseline_search_24), null, null, null);
            } else {
                inputEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.baseline_search_24), null, ContextCompat.getDrawable(this, R.drawable.baseline_clear_24), null);
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

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }
}