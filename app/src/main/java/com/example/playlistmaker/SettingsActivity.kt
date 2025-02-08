package com.example.playlistmaker
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        val backButton = findViewById<MaterialButton>(R.id.button_back)

        backButton?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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

        val linkButton=findViewById<MaterialButton>(R.id.link_button)
        linkButton.setOnClickListener{
            val link = getString(R.string.link)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(link)
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

}