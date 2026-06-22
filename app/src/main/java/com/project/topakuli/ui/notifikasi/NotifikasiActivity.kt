package com.project.topakuli.ui.notifikasi

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
        
        viewModel.syncNotifications()
        viewModel.markAllAsRead()
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
            viewModel.allNotifications.collectLatest { list ->
                (binding.rvNotifikasi.adapter as NotificationAdapter).submitList(list)
            }
        }
    }
}
