package com.project.topakuli.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.project.topakuli.R
import com.project.topakuli.databinding.ActivityAdminMainBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.data.remote.model.ContributionForm

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private var unreadListener: com.google.firebase.firestore.ListenerRegistration? = null

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

        setupUnreadBadge()
    }

    private fun setupUnreadBadge() {
        val firestore = FirebaseFirestore.getInstance()
        val prefs = getSharedPreferences("topakuli_admin_notif_prefs", MODE_PRIVATE)
        
        unreadListener = firestore.collection("user_contributions")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                
                val isNotifEnabled = prefs.getBoolean("notifications_enabled", true)
                
                // Hitung data yang belum dibaca (field isReadByAdmin false atau tidak ada)
                val unreadCount = snapshot.documents.count { doc ->
                    !(doc.getBoolean("isReadByAdmin") ?: false)
                }
                
                val badge = binding.adminNavView.getOrCreateBadge(R.id.admin_navigation_survei)
                if (unreadCount > 0 && isNotifEnabled) {
                    badge.isVisible = true
                    // Titik hijau saja tanpa angka sesuai permintaan
                    badge.backgroundColor = androidx.core.content.ContextCompat.getColor(this, R.color.hijau_topakuli)
                } else {
                    badge.isVisible = false
                }

                // Jika baru pertama kali dengar atau ada perubahan, bisa munculkan system notification juga di sini
                // Tapi untuk sekarang kita fokus ke badge navbar sesuai permintaan sebelumnya.
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        unreadListener?.remove()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_admin) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
