package com.example.playlistmaker.presentation.ui.root

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityRootBinding
import com.example.playlistmaker.presentation.ui.fragments.PlaylistFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class RootActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRootBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // В коде активности/фрагмента
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.itemTextAppearanceActive = R.style.BottomNavTheme
        bottomNavigation.itemTextAppearanceInactive = R.style.BottomNavTheme

        setupNavigation()
    }

    fun hideBottomNavigation() {
        binding.bottomNavigation.visibility = View.GONE
    }

    fun showBottomNavigation() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }

    private fun setupNavigation() {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)

            // Безопасное приведение типа
            if (navHostFragment is NavHostFragment) {
                navController = navHostFragment.navController

                // Восстанавливаем граф, если он не установлен
                if (navController.graph.id == 0) {
                    navController.setGraph(R.navigation.nav_graph)
                }

                binding.bottomNavigation.setupWithNavController(navController)

                navController.addOnDestinationChangedListener { _, destination, _ ->
                    binding.bottomNavigation.visibility = when (destination.id) {
                        R.id.track_player -> View.GONE
                        R.id.newPlaylistFragment -> View.GONE
                        R.id.fragment_playlist -> View.GONE
                        else -> View.VISIBLE
                    }
                }
            } else {
                restoreNavigation()
            }
        } catch (e: Exception) {
            Log.e("RootActivity", "Error setting up navigation", e)
            restoreNavigation()
        }
    }
    // В RootActivity добавьте:
    override fun onResume() {
        super.onResume()
        // Принудительно скрываем навигацию если текущий фрагмент - PlaylistFragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager?.fragments?.firstOrNull()

        if (currentFragment is PlaylistFragment) {
            binding.bottomNavigation.visibility = View.GONE
        }
    }

    private fun restoreNavigation() {
        try {
            // Создаем новый NavHostFragment
            val navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, navHostFragment)
                .setPrimaryNavigationFragment(navHostFragment)
                .commitNow()

            // Повторно инициализируем навигацию
            navController = navHostFragment.navController
            binding.bottomNavigation.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                binding.bottomNavigation.visibility = when (destination.id) {
                    R.id.track_player -> View.GONE
                    R.id.newPlaylistFragment -> View.GONE
                    R.id.fragment_playlist -> View.GONE
                    else -> View.VISIBLE
                }
            }
        } catch (e: Exception) {
            Log.e("RootActivity", "Error restoring navigation", e)
            // В крайнем случае перезапускаем активность
            restartActivity()
        }
    }

    private fun restartActivity() {
        val intent = Intent(this, RootActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}