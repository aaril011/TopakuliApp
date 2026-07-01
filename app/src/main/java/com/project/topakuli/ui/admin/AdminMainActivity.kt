package com.project.topakuli.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.topakuli.R
import com.project.topakuli.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Memastikan status bar berwarna hijau dan ikon putih
        window.apply {
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = androidx.core.content.ContextCompat.getColor(this@AdminMainActivity, R.color.hijau_topakuli)
            androidx.core.view.WindowCompat.getInsetsController(this, decorView).isAppearanceLightStatusBars = false
        }

        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_admin) as NavHostFragment
        val navController = navHostFragment.navController
        binding.adminNavView.setupWithNavController(navController)

        // Memastikan klik pada item navbar melakukan navigasi yang benar tanpa menumpuk fragment
        binding.adminNavView.setOnItemSelectedListener { item ->
            if (item.itemId != navController.currentDestination?.id) {
                navController.navigate(item.itemId)
            }
            true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.admin_navigation_beranda,
                R.id.admin_navigation_herbal,
                R.id.admin_navigation_resep,
                R.id.admin_navigation_survei,
                R.id.admin_navigation_setelan -> {
                    binding.adminNavView.visibility = View.VISIBLE
                }
                else -> {
                    binding.adminNavView.visibility = View.GONE
                }
            }
        }

        // Logic to hide BottomNav when keyboard is visible to prevent shaking and "pushed up" look
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val currentDest = navController.currentDestination?.id

            val shouldBeVisible = when (currentDest) {
                R.id.admin_navigation_beranda,
                R.id.admin_navigation_herbal,
                R.id.admin_navigation_resep,
                R.id.admin_navigation_survei,
                R.id.admin_navigation_setelan -> true
                else -> false
            }

            if (shouldBeVisible) {
                binding.adminNavView.visibility = if (isKeyboardVisible) View.GONE else View.VISIBLE
            }
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_admin) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
