package com.project.topakuli.ui.admin.resep

import android.os.Bundle
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.databinding.FragmentAdminAddResepBinding
import com.project.topakuli.util.AdminLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AdminAddResepFragment : Fragment() {

    private var _binding: FragmentAdminAddResepBinding? = null
    private val binding get() = _binding!!
    private var resepId: String? = null
    
    private var selectedTanamanIds = mutableListOf<String>()
    private var listTanaman: List<TanamanObatEntity> = emptyList()
    private var mainImageUri: android.net.Uri? = null
    private var isKeyboardVisible = false
    private var lastFocusedView: View? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            mainImageUri = it
            binding.layoutPlaceholder.visibility = View.GONE
            binding.ivPreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(binding.ivPreview)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAddResepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resepId = arguments?.getString("EXTRA_RESEP_ID")
        if (resepId != null) {
            binding.toolbar.title = "Edit Resep"
            binding.btnDelete.visibility = View.VISIBLE
            loadResepData(resepId!!)
        }

        setupActions()
        setupCategorySpinner()
        setupKeyboardScrolling()
        loadTanamanList()
    }

    private fun setupKeyboardScrolling() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            if (_binding == null) return@addOnGlobalLayoutListener

            val r = android.graphics.Rect()
            binding.root.getWindowVisibleDisplayFrame(r)
            val screenHeight = binding.root.rootView.height
            val keypadHeight = screenHeight - r.bottom

            val nowVisible = keypadHeight > screenHeight * 0.15
            val currentFocus = activity?.currentFocus

            if (nowVisible) {
                if (!isKeyboardVisible || (currentFocus != null && currentFocus != lastFocusedView)) {
                    isKeyboardVisible = true
                    lastFocusedView = currentFocus
                    
                    if (currentFocus is EditText) {
                        binding.scrollViewAdminResep.post {
                            val rect = android.graphics.Rect()
                            currentFocus.getDrawingRect(rect)
                            binding.scrollViewAdminResep.offsetDescendantRectToMyCoords(currentFocus, rect)
                            binding.scrollViewAdminResep.smoothScrollTo(0, Math.max(0, rect.top - 150))
                        }
                    }
                }
            } else {
                if (isKeyboardVisible) {
                    isKeyboardVisible = false
                    lastFocusedView = null
                    binding.scrollViewAdminResep.smoothScrollTo(0, 0)
                }
            }
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            "Penyakit Dalam",
            "Pencernaan",
            "Pernapasan",
            "Tulang, Sendi & Otot",
            "Kulit & Luka",
            "Kesehatan Wanita & Ibu",
            "Ginjal & Saluran Kemih",
            "Kesehatan Umum"
        )

        val categoryDetails = mapOf(
            "Penyakit Dalam" to "Contoh: Hipertensi, kolesterol, asam urat, diabetes, anemia",
            "Pencernaan" to "Contoh: Maag, diare, sembelit, perut kembung, mual, masuk angin",
            "Pernapasan" to "Contoh: Batuk, pilek, flu, radang tenggorokan, asma ringan",
            "Tulang, Sendi & Otot" to "Contoh: Rematik, pegal linu, nyeri sendi, sakit pinggang, keseleo",
            "Kulit & Luka" to "Contoh: Gatal, bisul, luka bakar ringan, luka sayat, kutu air",
            "Kesehatan Wanita & Ibu" to "Contoh: Nyeri haid, keputihan, nifas, pelancar ASI",
            "Ginjal & Saluran Kemih" to "Contoh: Batu ginjal, anyang-anyangan, susah buang air kecil",
            "Kesehatan Umum" to "Contoh: Demam, penambah stamina, daya tahan tubuh, penurun panas"
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_category, categories)
        (binding.etKategori as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            // Mengatur dropdown agar muncul di bawah input
            setDropDownBackgroundResource(R.drawable.bg_search_bar)

            setOnItemClickListener { _, _, position, _ ->
                val selected = categories[position]
                binding.tvContohKategori.text = categoryDetails[selected]
                binding.tvContohKategori.visibility = View.VISIBLE
            }
        }
    }

    private fun loadTanamanList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = (requireActivity().application as TopakuliApplication).repository
            listTanaman = repository.allTanaman.first()
        }
    }

    private fun showMultiSelectTanaman() {
        val names = listTanaman.map { it.nama }.toTypedArray()
        val checkedItems = BooleanArray(listTanaman.size) { i ->
            selectedTanamanIds.contains(listTanaman[i].id)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Tanaman Terkait")
            .setMultiChoiceItems(names, checkedItems) { _, which, isChecked ->
                val id = listTanaman[which].id
                if (isChecked) {
                    if (!selectedTanamanIds.contains(id)) selectedTanamanIds.add(id)
                } else {
                    selectedTanamanIds.remove(id)
                }
            }
            .setPositiveButton("Selesai") { _, _ ->
                updateChips()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateChips() {
        binding.cgTanamanTerkait.removeAllViews()
        selectedTanamanIds.forEach { id ->
            val tanaman = listTanaman.find { it.id == id }
            if (tanaman != null) {
                val chip = Chip(requireContext()).apply {
                    text = tanaman.nama
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        selectedTanamanIds.remove(id)
                        updateChips()
                    }
                }
                binding.cgTanamanTerkait.addView(chip)
            }
        }
    }

    private fun loadResepData(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = (requireActivity().application as TopakuliApplication).repository
            val resep = withContext(Dispatchers.IO) { repository.getResepById(id) }
            if (resep != null) {
                binding.apply {
                    etJudul.setText(resep.judul)
                    etKategori.setText(resep.kategori, false)
                    
                    val categoryDetails = mapOf(
                        "Penyakit Dalam" to "Contoh: Hipertensi, kolesterol, asam urat, diabetes, anemia",
                        "Pencernaan" to "Contoh: Maag, diare, sembelit, perut kembung, mual, masuk angin",
                        "Pernapasan" to "Contoh: Batuk, pilek, flu, radang tenggorokan, asma ringan",
                        "Tulang, Sendi & Otot" to "Contoh: Rematik, pegal linu, nyeri sendi, sakit pinggang, keseleo",
                        "Kulit & Luka" to "Contoh: Gatal, bisul, luka bakar ringan, luka sayat, kutu air",
                        "Kesehatan Wanita & Ibu" to "Contoh: Nyeri haid, keputihan, nifas, pelancar ASI",
                        "Ginjal & Saluran Kemih" to "Contoh: Batu ginjal, anyang-anyangan, susah buang air kecil",
                        "Kesehatan Umum" to "Contoh: Demam, penambah stamina, daya tahan tubuh, penurun panas"
                    )
                    
                    if (categoryDetails.containsKey(resep.kategori)) {
                        tvContohKategori.text = categoryDetails[resep.kategori]
                        tvContohKategori.visibility = View.VISIBLE
                    }

                    etDeskripsi.setText(resep.deskripsi)
                    etManfaat.setText(resep.manfaat)
                    etBahan.setText(resep.bahan)
                    etCara.setText(resep.caraPembuatan)
                    etWaktu.setText(resep.waktuPersiapan)
                    etCaraPakai.setText(resep.caraPakai)
                    etFrekuensi.setText(resep.frekuensi)
                    etSimpan.setText(resep.masaSimpan)
                    etKesulitan.setText(resep.tingkatKesulitan)
                    etCatatan.setText(resep.catatan)
                    
                    selectedTanamanIds = resep.tanamanIds?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
                    
                    // Wait until listTanaman is loaded to update chips
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (listTanaman.isEmpty()) {
                            listTanaman = repository.allTanaman.first()
                        }
                        updateChips()
                    }

                    if (!resep.imageUrl.isNullOrEmpty()) {
                        layoutPlaceholder.visibility = View.GONE
                        ivPreview.visibility = View.VISIBLE
                        Glide.with(requireContext())
                            .load(resep.imageUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(ivPreview)
                    }
                }
            }
        }
    }

    private fun setupActions() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnSave.setOnClickListener {
            saveResep()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnPickImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSelectTanaman.setOnClickListener {
            showMultiSelectTanaman()
        }

        setupAutoList(binding.etBahan)
        setupAutoList(binding.etCara)
    }

    private fun setupAutoList(editText: android.widget.EditText) {
        editText.addTextChangedListener(object : android.text.TextWatcher {
            private var isInternalChange = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isInternalChange) return
                
                // Detect newline addition (Enter pressed)
                // In onTextChanged, if 1 character is added and it's \n
                if (count == 1 && before == 0 && s?.get(start) == '\n') {
                    val text = s.toString()
                    
                    // Find the start of the line that was JUST finished (the line before the \n)
                    val lastLineEnd = start
                    var lastLineStart = text.lastIndexOf('\n', lastLineEnd - 1) + 1
                    if (lastLineStart < 0) lastLineStart = 0
                    
                    val lastLine = text.substring(lastLineStart, lastLineEnd)
                    
                    // Check if last line was a list item
                    val numberedMatch = Regex("^(\\d+)\\.\\s").find(lastLine)
                    val bulletMatch = Regex("^[•\\-\\*]\\s").find(lastLine)

                    if (numberedMatch != null || bulletMatch != null) {
                        // If user pressed Enter on a line that ONLY has the symbol, end the list
                        if (lastLine.trim() == numberedMatch?.value?.trim() || lastLine.trim() == bulletMatch?.value?.trim()) {
                            isInternalChange = true
                            editText.text.delete(lastLineStart, start + 1)
                            isInternalChange = false
                            return
                        }

                        val nextSymbol = if (numberedMatch != null) {
                            val nextNum = numberedMatch.groupValues[1].toInt() + 1
                            "$nextNum. "
                        } else {
                            bulletMatch?.value ?: "• "
                        }

                        isInternalChange = true
                        editText.text.insert(start + 1, nextSymbol)
                        isInternalChange = false
                    }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun saveResep() {
        val judul = binding.etJudul.text.toString().trim()
        val kategori = binding.etKategori.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val manfaat = binding.etManfaat.text.toString().trim()
        val bahan = binding.etBahan.text.toString().trim()
        val cara = binding.etCara.text.toString().trim()
        val waktu = binding.etWaktu.text.toString().trim()
        val caraPakai = binding.etCaraPakai.text.toString().trim()
        val frekuensi = binding.etFrekuensi.text.toString().trim()
        val simpan = binding.etSimpan.text.toString().trim()
        val kesulitan = binding.etKesulitan.text.toString().trim()
        val catatan = binding.etCatatan.text.toString().trim()

        if (judul.isEmpty() || kategori.isEmpty() || bahan.isEmpty() || cara.isEmpty()) {
            Toast.makeText(requireContext(), "Judul, Kategori, Bahan, dan Cara harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val app = (requireActivity().application as TopakuliApplication)
            val repo = app.repository
            
            Toast.makeText(appContext, "Sedang menyimpan...", Toast.LENGTH_SHORT).show()

            var existingResep: ResepEntity? = null
            if (resepId != null) {
                existingResep = withContext(Dispatchers.IO) { repo.getResepById(resepId!!) }
            }

            // Upload image to Supabase if changed
            var finalImageUrl: String? = null
            if (mainImageUri != null) {
                // Delete old image if exists
                if (resepId != null) {
                    existingResep?.imageUrl?.let { oldUrl ->
                        if (oldUrl.contains("supabase")) {
                            repo.deleteImage(oldUrl, "resep")
                        }
                    }
                }

                finalImageUrl = repo.uploadImage(mainImageUri!!, "resep")
                if (finalImageUrl == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(appContext, "Gagal mengunggah gambar ke Supabase. Periksa koneksi atau pengaturan bucket 'resep'", Toast.LENGTH_LONG).show()
                    }
                    // Jika upload gagal, kita batalkan simpan agar data tidak rusak (tanpa gambar)
                    return@launch
                }
                android.util.Log.d("AdminAddResep", "Image uploaded successfully: $finalImageUrl")
            } else {
                finalImageUrl = existingResep?.imageUrl
            }

            val finalResepId = resepId ?: UUID.randomUUID().toString()
            val resep = ResepEntity(
                id = finalResepId,
                judul = judul,
                deskripsi = deskripsi,
                bahan = bahan,
                caraPembuatan = cara,
                kategori = kategori,
                manfaat = manfaat,
                waktuPersiapan = waktu,
                caraPakai = caraPakai,
                frekuensi = frekuensi,
                masaSimpan = simpan,
                tingkatKesulitan = kesulitan.ifEmpty { "Menengah" },
                tanamanIds = selectedTanamanIds.joinToString(","),
                imageUrl = finalImageUrl,
                catatan = catatan,
                isSaved = existingResep?.isSaved ?: false,
                lastReadAt = existingResep?.lastReadAt,
                viewCount = existingResep?.viewCount ?: 0,
                createdAt = existingResep?.createdAt ?: System.currentTimeMillis()
            )

            withContext(Dispatchers.IO) {
                repo.insertResep(resep)
                AdminLogger.log(
                    appContext,
                    repo,
                    if (resepId == null) "Tambah" else "Ubah",
                    "Resep",
                    judul,
                    finalResepId
                )
            }
            
            com.project.topakuli.util.DialogHelper.showSuccessDialog(
                requireContext(),
                "Berhasil Disimpan",
                "Resep $judul telah berhasil diperbarui di database.",
                "Kembali ke Daftar"
            ) {
                findNavController().navigateUp()
            }
        }
    }

    private fun showDeleteConfirmation() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel)
        val btnDelete = dialogView.findViewById<View>(R.id.btn_confirm_delete)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            dialog.dismiss()
            deleteResep()
        }

        dialog.show()
    }

    private fun deleteResep() {
        val id = resepId ?: return
        val judul = binding.etJudul.text.toString()
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val repo = (requireActivity().application as TopakuliApplication).repository
                repo.deleteResep(id)
                AdminLogger.log(appContext, repo, "Hapus", "Resep", judul)
            }
            
            com.project.topakuli.util.DialogHelper.showSuccessDialog(
                requireContext(),
                "Berhasil Dihapus",
                "Resep $judul telah dihapus secara permanen.",
                "Selesai"
            ) {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
