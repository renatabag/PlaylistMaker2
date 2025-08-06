package com.example.playlistmaker.presentation.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.presentation.ui.fragments.MediaLibraryFragment
import com.example.playlistmaker.presentation.ui.activities.PlayListsFragment
import com.example.playlistmaker.presentation.ui.activities.TracksFragment

class MediaViewPagerAdapter(fragmentActivity: MediaLibraryFragment) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> TracksFragment.newInstance()
            1 -> PlayListsFragment.newInstance()
            else -> throw IllegalArgumentException()
        }
    }
}