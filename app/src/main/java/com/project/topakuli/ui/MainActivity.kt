package com.project.topakuli.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.topakuli.R
import com.project.topakuli.util.NotificationHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContentView(R.layout.activity_main)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        subscribeToNotificationTopic()

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_identifikasi, R.id.petunjukFragment, 
                R.id.tipsAmanFragment, R.id.tentangFragment, R.id.kontribusiFragment,
                R.id.detailTanamanFragment, R.id.detailResepFragment,
                R.id.historyFragment, R.id.historyDetailFragment -> {
                    navView.visibility = View.GONE
                }
                else -> {
                    navView.visibility = View.VISIBLE
                }
            }
        }

        // Logic to hide BottomNav when keyboard is visible to prevent shaking and "pushed up" look
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val currentDest = navController.currentDestination?.id
            
            // Destinations that should always have NavView GONE are handled by listener above
            val shouldBeVisible = when (currentDest) {
                R.id.navigation_identifikasi, R.id.petunjukFragment, 
                R.id.tipsAmanFragment, R.id.tentangFragment, R.id.kontribusiFragment,
                R.id.detailTanamanFragment, R.id.detailResepFragment,
                R.id.historyFragment, R.id.historyDetailFragment -> false
                else -> true
            }

            if (shouldBeVisible) {
                navView.visibility = if (isKeyboardVisible) View.GONE else View.VISIBLE
            }
            insets
        }
    }

    private fun subscribeToNotificationTopic() {
        val prefs = getSharedPreferences("topakuli_notif_prefs", MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        
        if (notificationsEnabled) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users")
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        android.util.Log.e("MainActivity", "Topic subscription failed")
                    } else {
                        android.util.Log.d("MainActivity", "Subscribed to all_users topic")
                    }
                }
        } else {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic("all_users")
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
    }
}
