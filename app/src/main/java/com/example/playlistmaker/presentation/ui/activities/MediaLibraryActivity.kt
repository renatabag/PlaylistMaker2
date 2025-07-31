package com.example.playlistmaker.presentation.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.playlistmaker.R.id.medialb


class MediaLibraryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media_library)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(medialb)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }
    }
}