package com.project.topakuli.ui.admin.common

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.remote.model.ContributionForm
import com.project.topakuli.databinding.FragmentAdminContributionDetailBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminContributionDetailFragment : Fragment() {

    private var _binding: FragmentAdminContributionDetailBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()

    private var contributionId: String? = null
    private var currentData: ContributionForm? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminContributionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Memastikan status bar berwarna hijau dan ikon putih
        activity?.window?.apply {
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.hijau_topakuli)
            
            // Nonaktifkan light status bar (buat ikon jadi putih)
            val decorView = decorView
            androidx.core.view.WindowCompat.getInsetsController(this, decorView).isAppearanceLightStatusBars = false
        }

        contributionId = arguments?.getString("CONTRIBUTION_ID")

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        loadDetail()
        setupDeleteButton()
    }

    private fun loadDetail() {
        val id = contributionId ?: return
        
        firestore.collection("user_contributions").document(id).get()
            .addOnSuccessListener { doc ->
                // Pastikan id diisi dari document ID jika di data class kosong
                val contribution = doc.toObject(ContributionForm::class.java)?.copy(id = doc.id)
                currentData = contribution
                contribution?.let { 
                    displayData(it)
                    // Mark as read if not already
                    if (!it.isReadByAdmin) {
                        markAsRead(id)
                    }
                }
            }
            .addOnFailureListener {
                if (isAdded) Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun markAsRead(id: String) {
        firestore.collection("user_contributions").document(id)
            .update("isReadByAdmin", true)
            .addOnSuccessListener {
                // Silently success, it will update stats in dashboard
            }
    }

    private fun displayData(data: ContributionForm) {
        with(binding) {
            tvDetailUserName.text = data.userName ?: "Anonim"
            tvDetailUserEmail.text = data.userEmail ?: "-"
            tvDetailUserPhone.text = data.userPhone ?: "-"
            tvDetailUserRole.text = data.userRole
            
            val content = SpannableStringBuilder()
            when(data.type) {
                "resep" -> {
                    tvDetailTypeBadge.text = "Informasi Resep Baru"
                    tvDetailMainTitle.text = data.recipeName ?: "Tanpa Judul"
                    
                    appendSection(content, "Keluhan", data.recipeSymptoms ?: "-")
                    appendSection(content, "Deskripsi", data.recipeDescription ?: "-")
                    appendSection(content, "Bahan-bahan", data.recipeIngredientsText ?: "-")
                    appendSection(content, "Cara Pembuatan", data.recipeStepsText ?: "-")
                    
                    appendSection(content, "Waktu Persiapan", data.recipePrepTime ?: "-")
                    appendSection(content, "Cara Pakai", data.recipeUsage ?: "-")
                    appendSection(content, "Frekuensi", data.recipeFrequency ?: "-")
                    appendSection(content, "Masa Simpan", data.recipeShelfLife ?: "-")
                    appendSection(content, "Efek Samping", data.recipeSideEffects ?: "-")
                    appendSection(content, "Referensi", data.recipeReference ?: "-")
                    
                    if (!data.recipeImageUrl.isNullOrEmpty()) {
                        cardDetailImage.visibility = View.VISIBLE
                        Glide.with(this@AdminContributionDetailFragment).load(data.recipeImageUrl).into(ivDetailImage)
                    } else {
                        cardDetailImage.visibility = View.GONE
                    }
                }
                "tanaman" -> {
                    tvDetailTypeBadge.text = "Informasi Tanaman Obat Baru"
                    tvDetailMainTitle.text = data.plantName ?: "Tanpa Nama"
                    
                    appendSection(content, "Nama Daerah", data.plantLocalName ?: "-")
                    appendSection(content, "Nama Latin", data.plantLatin ?: "-")
                    appendSection(content, "Khasiat Utama", data.plantBenefits ?: "-")
                    appendSection(content, "Deskripsi Lengkap", data.plantDescription ?: "-")
                    appendSection(content, "Referensi", data.plantReference ?: "-")

                    if (!data.plantImageUrl.isNullOrEmpty()) {
                        cardDetailImage.visibility = View.VISIBLE
                        Glide.with(this@AdminContributionDetailFragment).load(data.plantImageUrl).into(ivDetailImage)
                    } else {
                        cardDetailImage.visibility = View.GONE
                    }
                }
                "koreksi" -> {
                    tvDetailTypeBadge.text = "koreksi data"
                    tvDetailMainTitle.text = "Koreksi: ${data.targetItemName ?: "Data"}"
                    
                    appendSection(content, "Tipe Item", data.targetItemType?.replaceFirstChar { it.uppercase() } ?: "-")
                    appendSection(content, "Bagian Terkait", data.correctionPart ?: "-")
                    appendSection(content, "Penjelasan Koreksi", data.correctionNote ?: "-")
                    appendSection(content, "Sumber Referensi", data.correctionReference ?: "-")
                    cardDetailImage.visibility = View.GONE
                }
            }
            tvFullContent.text = content
        }
    }

    private fun appendSection(builder: SpannableStringBuilder, title: String, value: String) {
        val start = builder.length
        builder.append("$title\n")
        builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, start + title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.hijau_topakuli)), start, start + title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        
        builder.append("$value\n\n")
    }

    private fun setupDeleteButton() {
        binding.btnDeleteContribution.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
            val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            
            dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_title).text = "Hapus Ajuan"
            dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_message).text = "Apakah Anda yakin ingin menghapus ajuan ini secara permanen?"
            
            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
            dialogView.findViewById<View>(R.id.btn_confirm_delete).setOnClickListener {
                dialog.dismiss()
                deleteContribution()
            }
            
            dialog.show()
        }
    }

    private fun deleteContribution() {
        val id = contributionId ?: return
        val data = currentData ?: return
        
        // Activity log info
        val type = data.type
        val targetName = when(type) {
            "resep" -> data.recipeName ?: "Resep"
            "tanaman" -> data.plantName ?: "Tanaman"
            "koreksi" -> data.targetItemName ?: "Data"
            else -> "Ajuan"
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val app = (requireActivity().application as TopakuliApplication)
                val repository = app.repository

                // 1. Hapus gambar dari Supabase jika ada
                val imageUrl = if (type == "resep") data.recipeImageUrl else data.plantImageUrl
                val bucket = if (type == "resep") "pengajuan_resep" else "pengajuan_tanamanoabat"
                
                if (!imageUrl.isNullOrEmpty() && imageUrl.contains("supabase")) {
                    repository.deleteImage(imageUrl, bucket)
                }

                // 2. Hapus dari Firestore
                firestore.collection("user_contributions").document(id).delete().await()

                // 3. Log aktivitas hapus
                val admin = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val log = com.project.topakuli.data.local.entity.ActivityLog(
                    adminId = admin?.uid ?: "",
                    adminName = "Admin", // Fallback if name not loaded
                    type = "Hapus",
                    target = if (type == "koreksi") "Koreksi" else "Ajuan",
                    targetName = targetName,
                    timestamp = System.currentTimeMillis()
                )
                repository.logActivity(log)

                if (isAdded) {
                    com.project.topakuli.util.DialogHelper.showSuccessDialog(
                        requireContext(),
                        "Ajuan Dihapus",
                        "Data ajuan kontribusi telah berhasil dihapus secara permanen.",
                        "Kembali ke Daftar"
                    ) {
                        findNavController().navigateUp()
                    }
                }
            } catch (e: Exception) {
                if (isAdded) Toast.makeText(requireContext(), "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
