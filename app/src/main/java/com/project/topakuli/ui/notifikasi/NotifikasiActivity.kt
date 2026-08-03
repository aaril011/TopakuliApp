package com.project.topakuli.ui.notifikasi

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.databinding.ActivityNotifikasiBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotifikasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotifikasiBinding
    private val viewModel: NotifikasiViewModel by viewModels {
        NotifikasiViewModel.Factory((application as TopakuliApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifikasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
        
        val prefs = getSharedPreferences("TopakuliPrefs", MODE_PRIVATE)
        val installTime = prefs.getLong("install_timestamp", 0L)
        viewModel.fetchNotifications(installTime)
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        val adapter = NotificationAdapter()
        binding.rvNotifikasi.apply {
            layoutManager = LinearLayoutManager(this@NotifikasiActivity)
            this.adapter = adapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.notifications.collectLatest { list ->
                val isEmpty = list.isEmpty()
                binding.rvNotifikasi.visibility = if (isEmpty) View.GONE else View.VISIBLE
                binding.emptyStateNotifikasi.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
                
                if (isEmpty) {
                    binding.emptyStateNotifikasi.ivEmptyIllustration.setImageResource(R.drawable.ic_empty_notifications)
                    binding.emptyStateNotifikasi.tvEmptyTitle.text = "Belum ada notifikasi"
                    binding.emptyStateNotifikasi.tvEmptyDesc.text = "Anda akan menerima pemberitahuan jika ada perubahan data dari admin."
                    binding.emptyStateNotifikasi.btnRetry.visibility = View.GONE
                }
                
                (binding.rvNotifikasi.adapter as? NotificationAdapter)?.submitList(list)
            }
        }
    }
}
