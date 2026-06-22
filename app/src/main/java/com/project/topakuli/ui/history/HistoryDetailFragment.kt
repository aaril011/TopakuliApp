package com.project.topakuli.ui.history

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.databinding.FragmentHistoryDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryDetailFragment : Fragment() {

    private var _binding: FragmentHistoryDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nama = arguments?.getString("nama") ?: ""
        val akurasi = arguments?.getFloat("akurasi") ?: 0f
        val timestamp = arguments?.getLong("timestamp") ?: 0L
        val imagePath = arguments?.getString("imagePath")
        val tanamanId = arguments?.getString("tanamanId")

        binding.tvDetailName.text = nama
        binding.tvDetailAccuracy.text = String.format("Akurasi : %.1f%%", akurasi)
        binding.tvDetailDate.text = formatTimestamp(timestamp)

        Glide.with(this)
            .load(imagePath)
            .into(binding.ivDetailImage)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSeeDetail.setOnClickListener {
            if (tanamanId != null) {
                val bundle = Bundle().apply { putString("tanamanId", tanamanId) }
                findNavController().navigate(R.id.detailTanamanFragment, bundle)
            }
        }

        binding.btnReidentify.setOnClickListener {
            findNavController().navigate(R.id.navigation_identifikasi)
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Hasil Identifikasi Topakuli: $nama dengan akurasi ${String.format("%.1f%%", akurasi)}")
            }
            startActivity(Intent.createChooser(shareIntent, "Bagikan hasil"))
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID"))
        val timeString = timeFormat.format(Date(timestamp))

        return when {
            DateUtils.isToday(timestamp) -> {
                "Hari ini pukul $timeString"
            }
            DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS) -> {
                "Kemarin pukul $timeString"
            }
            else -> {
                val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
                dateFormat.format(Date(timestamp))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
