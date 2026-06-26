package com.project.topakuli.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.R
import com.project.topakuli.util.NotificationHelper

class MainActivity : AppCompatActivity() {

    private var notificationListener: com.google.firebase.firestore.ListenerRegistration? = null

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

        setupNotifications()

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
    }

    private fun setupNotifications() {
        val firestore = FirebaseFirestore.getInstance()
        val prefs = getSharedPreferences("topakuli_notif_prefs", MODE_PRIVATE)
        
        // Cek apakah notifikasi diaktifkan oleh user
        val isEnabled = prefs.getBoolean("notifications_enabled", true)
        if (!isEnabled) {
            android.util.Log.d("NotificationListener", "Notifications are disabled by user")
            return
        }

        // Use a 5-minute buffer for the first check to catch notifications sent just before opening
        val defaultTime = System.currentTimeMillis() - (5 * 60 * 1000)
        var lastCheckTime = prefs.getLong("last_notif_timestamp", defaultTime)
        
        android.util.Log.d("NotificationListener", "Starting listener from timestamp: $lastCheckTime")
        
        notificationListener = firestore.collection("public_notifications")
            .whereGreaterThan("timestamp", lastCheckTime)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("NotificationListener", "Listen failed.", e)
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener
                
                android.util.Log.d("NotificationListener", "Received snapshot with ${snapshot.documentChanges.size} changes")
                
                if (snapshot.metadata.hasPendingWrites()) return@addSnapshotListener

                var latestTimestamp = lastCheckTime

                snapshot.documentChanges.forEach { diff ->
                    if (diff.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val title = diff.document.getString("title") ?: "Pemberitahuan Baru"
                        val content = diff.document.getString("content") ?: ""
                        val timestamp = diff.document.getLong("timestamp") ?: 0L
                        
                        android.util.Log.d("NotificationListener", "Processing notification: $title (ts: $timestamp)")
                        
                        if (timestamp > lastCheckTime) {
                            val uniqueId = diff.document.id.hashCode()
                            NotificationHelper.showSystemNotification(this, title, content, uniqueId)
                            
                            if (timestamp > latestTimestamp) {
                                latestTimestamp = timestamp
                            }
                        }
                    }
                }
                
                if (latestTimestamp > lastCheckTime) {
                    lastCheckTime = latestTimestamp
                    prefs.edit().putLong("last_notif_timestamp", latestTimestamp).apply()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationListener?.remove()
    }
}
