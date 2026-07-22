package com.project.topakuli.ui.admin.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.topakuli.databinding.FragmentAdminHelpBinding

class AdminHelpFragment : Fragment() {

    private var _binding: FragmentAdminHelpBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var faqAdapter: FaqAdapter
    private val fullFaqList = listOf(
        FaqItem("Bagaimana cara menambahkan tanaman baru?", "Buka menu Tanaman Herbal, tap tombol + di pojok kanan bawah, lalu isi formulir lengkap dan simpan."),
        FaqItem("Bagaimana cara menyetujui koreksi data?", "Masuk ke menu Masukan Masyarakat, pilih data yang ingin ditinjau, periksa kebenarannya, lalu tekan tombol Setujui di bagian bawah."),
        FaqItem("Siapa yang bisa mengajukan koreksi?", "Seluruh pengguna aplikasi masyarakat dapat mengajukan koreksi melalui menu kontribusi di pengaturan mereka setelah mengisi identitas singkat."),
        FaqItem("Bagaimana alur identifikasi tanaman?", "Pengguna mengambil foto daun via kamera, sistem AI (ResNet-50) memproses, dan menampilkan hasil berupa nama serta khasiat tanaman."),
        FaqItem("Apakah data statistik bersifat riil?", "Ya, data laporan statistik ditarik langsung dari Firebase Firestore yang mencatat aktivitas pengelola dan penggunaan masyarakat secara real-time."),
        FaqItem("Bagaimana cara menghubungi pihak Puskesmas?", "Pengguna umum dapat menekan tombol 'Tanya lewat WhatsApp' pada halaman Tips Herbal Aman untuk terhubung ke nomor resmi Puskesmas."),
        FaqItem("Apa fungsi algoritma Levenshtein Distance?", "Algoritma ini digunakan pada fitur pencarian untuk menangani kesalahan pengetikan (typo) agar pengguna tetap menemukan resep yang relevan."),
        FaqItem("Bagaimana keamanan data admin terjaga?", "Akses dashboard admin dilindungi oleh Firebase Authentication, hanya akun terdaftar dengan kredensial valid yang dapat mengelola database.")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupActions()
        setupSearch()
    }

    private fun setupRecyclerView() {
        faqAdapter = FaqAdapter()
        binding.rvFaq.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = faqAdapter
        }
        // Munculkan tetap 3 poin awal sesuai permintaan
        faqAdapter.submitList(fullFaqList.take(3))
    }

    private fun setupSearch() {
        binding.etSearchBantuan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSearchBantuan.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.text.toString())
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        val filtered = if (query.isEmpty()) {
            fullFaqList.take(3) // Kembali ke 3 poin awal jika kosong
        } else {
            fullFaqList.filter { 
                it.question.contains(query, ignoreCase = true) || 
                it.answer.contains(query, ignoreCase = true) 
            }
        }

        faqAdapter.submitList(filtered)
        binding.tvNoFaqResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvFaq.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupActions() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnVideoTutorial.setOnClickListener {
            val videoUrl = "https://youtu.be/wc0_O-1oSbg?si=H2alSIhf81eqKSpt"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal membuka video tutorial", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnPanduanPengelola.setOnClickListener {
            val guideUrl = "https://drive.google.com/file/d/1qHzXdQfs94jfCT7W4MQbJLLBGU0mmavL/view?usp=sharing"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(guideUrl))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal membuka panduan pengelola", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}