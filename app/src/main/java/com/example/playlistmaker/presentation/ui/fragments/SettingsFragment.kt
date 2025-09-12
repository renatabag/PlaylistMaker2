package com.example.playlistmaker.presentation.ui.fragments

import android.R
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.SettingsBinding
import com.example.playlistmaker.presentation.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {
    private var _binding: SettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        setupThemeSwitcher()
        setupButtons()
    }

    private fun setupThemeSwitcher() {
        val switchMaterial = binding.themeSwitcher

        viewModel.isDarkTheme.observe(viewLifecycleOwner) { isDarkTheme ->
            switchMaterial.isChecked = isDarkTheme
            applyTheme(isDarkTheme)
        }

        switchMaterial.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        val states = arrayOf(
            intArrayOf(R.attr.state_checked),
            intArrayOf(-R.attr.state_checked)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(requireContext(), com.example.playlistmaker.R.color.blue_switch),
            ContextCompat.getColor(requireContext(), com.example.playlistmaker.R.color.grey_switch),
        )
        val colorsTrack = intArrayOf(
            ContextCompat.getColor(requireContext(), com.example.playlistmaker.R.color.blue_switch_light),
            ContextCompat.getColor(requireContext(), com.example.playlistmaker.R.color.grey_switch_light)
        )

        switchMaterial.thumbTintList = ColorStateList(states, colors)
        switchMaterial.trackTintList = ColorStateList(states, colorsTrack)
    }

    private fun setupButtons() {

        binding.buttonShareSettings.setOnClickListener {
            val shareIntent = shareApp()
            startActivity(Intent.createChooser(shareIntent, getString(com.example.playlistmaker.R.string.share_app)))
        }

        binding.supportButton.setOnClickListener {
            val developerEmail = getString(com.example.playlistmaker.R.string.developer_email)
            val subject = getString(com.example.playlistmaker.R.string.support_email_subject)
            val body = getString(com.example.playlistmaker.R.string.support_email_body)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(developerEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        }

        binding.linkButton.setOnClickListener {
            val link = getString(com.example.playlistmaker.R.string.link)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
        }
    }

    private fun shareApp(): Intent {
        val shareText = getString(com.example.playlistmaker.R.string.adress_practicum)
        return Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        binding.themeSwitcher.jumpDrawablesToCurrentState()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}