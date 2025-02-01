package com.example.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val button1 = findViewById<MaterialButton>(R.id.button1)
        val button2 = findViewById<MaterialButton>(R.id.button2)
        val button3 = findViewById<MaterialButton>(R.id.button3)

        button1?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(this@MainActivity, "Нажата кнопка ПОИСК", Toast.LENGTH_SHORT).show()
            }
        })

        button2?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажата кнопка МЕДИАТЕКА", Toast.LENGTH_SHORT).show()
        }

        button3?.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажата кнопка НАСТРОЙКИ", Toast.LENGTH_SHORT).show()
        }
    }
}