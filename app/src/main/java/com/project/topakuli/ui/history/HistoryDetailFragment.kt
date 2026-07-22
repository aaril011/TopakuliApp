package com.project.topakuli.ui.history

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
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
    private var isUiVisible = true

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
        val latin = arguments?.getString("namaLatin") ?: ""
        val daerah = arguments?.getString("namaDaerah") ?: ""
        val akurasi = arguments?.getFloat("akurasi") ?: 0f
        val timestamp = arguments?.getLong("timestamp") ?: 0L
        val imagePath = arguments?.getString("imagePath")
        val tanamanId = arguments?.getString("tanamanId")

        binding.tvDetailName.text = nama
        binding.tvDetailLatin.text = latin
        binding.tvDetailDaerah.text = daerah.ifEmpty { "-" }
        binding.tvAkurasiTanaman.text = String.format("%.2f%%", akurasi)
        binding.tvDetailDate.text = formatTimestamp(timestamp)

        Glide.with(this)
            .load(imagePath)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(binding.ivDetailImage)

        // Logika Animasi Toggle saat Gambar diklik
        binding.ivDetailImage.setOnClickListener {
            toggleUiVisibility()
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSeeDetail.setOnClickListener {
            if (!tanamanId.isNullOrEmpty()) {
                val bundle = Bundle().apply { putString("tanamanId", tanamanId) }
                findNavController().navigate(R.id.detailTanamanFragment, bundle)
            } else {
                showDataNotFoundDialog()
            }
        }

        binding.btnReidentify.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.navigation_beranda, false)
                .build()
            findNavController().navigate(R.id.navigation_identifikasi, null, navOptions)
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Hasil Identifikasi Topakuli: $nama dengan akurasi ${String.format("%.2f%%", akurasi)}")
            }
            startActivity(Intent.createChooser(shareIntent, "Bagikan hasil"))
        }
    }

    private fun showDataNotFoundDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_success_custom, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Customizing generic success dialog for "not found" state
        val layoutHeader = dialogView.findViewById<View>(R.id.layout_header)
        val cardIconBg = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_icon_bg)
        val ivIcon = dialogView.findViewById<android.widget.ImageView>(R.id.iv_dialog_icon)
        val tvTitle = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_title)
        val tvMessage = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_message)
        val btnAction = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_action)

        layoutHeader.setBackgroundColor(android.graphics.Color.parseColor("#FFFBEB")) // Light orange
        cardIconBg.setCardBackgroundColor(android.graphics.Color.parseColor("#F59E0B")) // Orange
        ivIcon.setImageResource(R.drawable.info)
        tvTitle.text = "Data Belum Tersedia"
        tvTitle.setTextColor(android.graphics.Color.parseColor("#92400E"))
        tvMessage.text = "Mohon maaf, informasi detail untuk tanaman ini belum tersedia di database kami."
        btnAction.text = "Lihat Daftar Tanaman Obat"
        btnAction.setBackgroundColor(android.graphics.Color.parseColor("#F59E0B"))

        btnAction.setOnClickListener {
            dialog.dismiss()
            // Kembali ke root beranda dulu sebelum pindah tab agar state navigasi bersih
            findNavController().popBackStack(R.id.navigation_beranda, false)
            
            // Mengalihkan tab ke Herbal melalui BottomNavigationView agar state Navbar sinkron
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.nav_view)
                ?.selectedItemId = R.id.navigation_herbal
        }

        dialog.show()
    }

    private fun toggleUiVisibility() {
        isUiVisible = !isUiVisible
        
        val interpolator = AccelerateDecelerateInterpolator()
        val duration = 400L

        if (isUiVisible) {
            // Animasi Menampilkan (Kembali ke posisi awal)
            binding.headerDetail.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()

            binding.cvResult.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
                
            binding.vGradient.animate()
                .alpha(1f)
                .setDuration(duration)
                .start()
        } else {
            // Animasi Menghilangkan
            // Header naik ke atas
            binding.headerDetail.animate()
                .translationY(-binding.headerDetail.height.toFloat() - 100f)
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()

            // Card turun ke bawah
            binding.cvResult.animate()
                .translationY(binding.cvResult.height.toFloat() + 100f)
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
                
            binding.vGradient.animate()
                .alpha(0f)
                .setDuration(duration)
                .start()
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
