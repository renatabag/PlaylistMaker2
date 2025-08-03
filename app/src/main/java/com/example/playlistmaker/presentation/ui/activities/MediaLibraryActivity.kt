package com.example.playlistmaker.presentation.ui.activities

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaLibraryBinding
import com.example.playlistmaker.presentation.ui.adapters.MediaViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MediaLibraryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMediaLibraryBinding
    private lateinit var tabMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMediaLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = MediaViewPagerAdapter(this)

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> getString(R.string.tab_favorites)
                1 -> getString(R.string.tab_playlists)
                else -> ""
            }
        }
        val displayMetrics = resources.displayMetrics
        val tabWidth = displayMetrics.widthPixels / 2 // Для 2 табов

        val tabs = (binding.tabLayout.getChildAt(0) as? ViewGroup)
        tabs?.let {
            for (i in 0 until it.childCount) {
                val tab = it.getChildAt(i)
                tab.layoutParams.width = tabWidth
                tab.requestLayout()
            }
        }
        tabMediator.attach()
        binding.buttonBack.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        tabMediator.detach()
    }
}