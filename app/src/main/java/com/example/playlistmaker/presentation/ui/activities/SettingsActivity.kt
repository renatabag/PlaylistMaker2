package com.example.playlistmaker.presentation.ui.activities
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.viewmodels.SettingsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel


class SettingsActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModel()

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        val switchMaterial: SwitchMaterial = findViewById(R.id.themeSwitcher)
        viewModel.isDarkTheme.observe(this) { isDarkTheme ->
            switchMaterial.isChecked = isDarkTheme
            applyTheme(isDarkTheme)
        }
        switchMaterial.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }


        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.blue_switch),
            ContextCompat.getColor(this, R.color.grey_switch),
        )
        val colorsTrack = intArrayOf(
            ContextCompat.getColor(this, R.color.blue_switch_light),
            ContextCompat.getColor(this, R.color.grey_switch_light)
        )

        val thumbColorStateList = ColorStateList(states, colors)
        val trackColorStateList = ColorStateList(states, colorsTrack)
        switchMaterial.thumbTintList = thumbColorStateList
        switchMaterial.trackTintList = trackColorStateList

        val backButton = findViewById<MaterialButton>(R.id.button_back)
        backButton?.setOnClickListener {
            finish()
        }

        val shareButton = findViewById<MaterialButton>(R.id.button_share_settings)
        shareButton.setOnClickListener {
            val shareIntent = shareApp()
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }

        val supportButton = findViewById<MaterialButton>(R.id.support_button)
        supportButton?.setOnClickListener {
            val developerEmail = getString(R.string.developer_email)
            val subject = getString(R.string.support_email_subject)
            val body = getString(R.string.support_email_body)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(developerEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        val linkButton = findViewById<MaterialButton>(R.id.link_button)
        linkButton.setOnClickListener {
            val link = getString(R.string.link)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("mailto:")
            }
            startActivity(intent)
        }
    }

    private fun shareApp(): Intent {
        val shareText = getString(R.string.adress_practicum)
        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
    }
    private fun applyTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}