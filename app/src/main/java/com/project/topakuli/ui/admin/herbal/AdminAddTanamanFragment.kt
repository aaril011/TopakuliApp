package com.project.topakuli.ui.admin.herbal

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanImageEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.databinding.FragmentAdminAddTanamanBinding
import com.project.topakuli.util.AdminLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AdminAddTanamanFragment : Fragment() {

    private var _binding: FragmentAdminAddTanamanBinding? = null
    private val binding get() = _binding!!
    private var tanamanId: String? = null
    
    private var selectedResepIds = mutableListOf<String>()
    private var listResep: List<ResepEntity> = emptyList()

    private var mainImageUri: Uri? = null
    private lateinit var additionalPhotosAdapter: AdditionalPhotosAdapter
    private var initialGalleryImages: List<TanamanImageEntity> = emptyList()
    private var isKeyboardVisible = false
    private var lastFocusedView: View? = null

    private val pickMainImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            mainImageUri = uri
            binding.ivPreview.visibility = View.VISIBLE
            binding.layoutPlaceholder.visibility = View.GONE
            binding.ivPreview.setImageURI(uri)
        }
    }

    private val pickAdditionalImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            additionalPhotosAdapter.addPhoto(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminAddTanamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        additionalPhotosAdapter = AdditionalPhotosAdapter { position ->
            additionalPhotosAdapter.removePhoto(position)
        }

        binding.rvAdditionalPhotos.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = additionalPhotosAdapter
        }

        tanamanId = arguments?.getString("EXTRA_TANAMAN_ID")
        if (tanamanId != null) {
            binding.toolbar.title = "Edit Tanaman"
            binding.btnDelete.visibility = View.VISIBLE
            loadTanamanData(tanamanId!!)
        }

        setupActions()
        setupKeyboardScrolling()
        loadResepList()
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
                        binding.scrollViewAdminTanaman.post {
                            val rect = android.graphics.Rect()
                            currentFocus.getDrawingRect(rect)
                            binding.scrollViewAdminTanaman.offsetDescendantRectToMyCoords(currentFocus, rect)
                            binding.scrollViewAdminTanaman.smoothScrollTo(0, Math.max(0, rect.top - 150))
                        }
                    }
                }
            } else {
                if (isKeyboardVisible) {
                    isKeyboardVisible = false
                    lastFocusedView = null
                    binding.scrollViewAdminTanaman.smoothScrollTo(0, 0)
                }
            }
        }
    }

    private fun loadResepList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = (requireActivity().application as TopakuliApplication).repository
            listResep = repository.allResep.first()
        }
    }

    private fun loadTanamanData(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = (requireActivity().application as TopakuliApplication).repository
            val tanaman = withContext(Dispatchers.IO) { repository.getTanamanById(id) }
            if (tanaman != null) {
                binding.apply {
                    etNama.setText(tanaman.nama)
                    etNamaDaerah.setText(tanaman.namaDaerah)
                    etNamaLatin.setText(tanaman.namaLatin)
                    etDeskripsi.setText(tanaman.deskripsi)
                    etKhasiat.setText(tanaman.khasiat)
                    
                    if (!tanaman.imageUrl.isNullOrEmpty()) {
                        layoutPlaceholder.visibility = View.GONE
                        ivPreview.visibility = View.VISIBLE
                        Glide.with(requireContext())
                            .load(tanaman.imageUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(ivPreview)
                    }
                }
            }

            // Load Additional Images
            initialGalleryImages = repository.getTanamanImages(id).first()
            val uris = initialGalleryImages.map { Uri.parse(it.imageUrl) }
            additionalPhotosAdapter.setPhotos(uris)

            // Load Related Recipes
            if (listResep.isEmpty()) {
                listResep = repository.allResep.first()
            }
            
            selectedResepIds = listResep.filter { it.tanamanIds?.contains(id) == true }.map { it.id }.toMutableList()
            updateChips()
        }
    }

    private fun updateChips() {
        binding.cgResepTerkait.removeAllViews()
        selectedResepIds.forEach { id ->
            val resep = listResep.find { it.id == id }
            if (resep != null) {
                val chip = Chip(requireContext()).apply {
                    text = resep.judul
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        selectedResepIds.remove(id)
                        updateChips()
                    }
                }
                binding.cgResepTerkait.addView(chip)
            }
        }
    }

    private fun setupActions() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnSave.setOnClickListener {
            saveTanaman()
        }

        binding.btnSelectResep.setOnClickListener {
            showMultiSelectResep()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnPickImage.setOnClickListener {
            pickMainImage.launch("image/*")
        }

        binding.btnAddOtherPhoto.setOnClickListener {
            pickAdditionalImage.launch("image/*")
        }
    }

    private fun saveTanaman() {
        val nama = binding.etNama.text.toString().trim()
        val namaDaerah = binding.etNamaDaerah.text.toString().trim()
        val namaLatin = binding.etNamaLatin.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val khasiat = binding.etKhasiat.text.toString().trim()

        if (nama.isEmpty() || namaDaerah.isEmpty() || namaLatin.isEmpty() || deskripsi.isEmpty() || khasiat.isEmpty()) {
            Toast.makeText(requireContext(), "Semua data tanaman harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val app = (requireActivity().application as TopakuliApplication)
            val repository = app.repository
            
            Toast.makeText(appContext, "Sedang menyimpan...", Toast.LENGTH_SHORT).show()
            
            val currentTanamanId = tanamanId ?: UUID.randomUUID().toString()
            var finalMainImageUrl: String? = null
            
            // 1. Upload Main Image to Supabase if changed
            if (mainImageUri != null) {
                // Delete old image if exists
                if (tanamanId != null) {
                    val existing = withContext(Dispatchers.IO) { repository.getTanamanById(tanamanId!!) }
                    existing?.imageUrl?.let { oldUrl ->
                        if (oldUrl.contains("supabase")) {
                            repository.deleteImage(oldUrl, "tanaman")
                        }
                    }
                }

                finalMainImageUrl = repository.uploadImage(mainImageUri!!, "tanaman")
                if (finalMainImageUrl == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(appContext, "Gagal mengunggah gambar utama", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                // Keep old one if editing
                if (tanamanId != null) {
                    val existing = withContext(Dispatchers.IO) { repository.getTanamanById(tanamanId!!) }
                    finalMainImageUrl = existing?.imageUrl
                }
            }

            val tanaman = TanamanObatEntity(
                id = currentTanamanId,
                nama = nama,
                namaDaerah = namaDaerah,
                namaLatin = namaLatin,
                deskripsi = deskripsi,
                khasiat = khasiat,
                imageUrl = finalMainImageUrl,
                createdAt = System.currentTimeMillis()
            )

            withContext(Dispatchers.IO) {
                repository.insertTanaman(tanaman)
                
                // 2. Handle Additional Images (Gallery)
                val currentPhotos = additionalPhotosAdapter.getPhotos()
                val currentPhotoStrings = currentPhotos.map { it.toString() }
                
                // A. Delete removed photos from Supabase and Database
                initialGalleryImages.forEach { oldImg ->
                    val isStillPresent = currentPhotoStrings.any { 
                        it.equals(oldImg.imageUrl, ignoreCase = true) || 
                        it.contains(oldImg.imageUrl.substringAfterLast("/")) 
                    }
                    
                    if (!isStillPresent) {
                        android.util.Log.d("AdminAddTanaman", "Removing gallery image from storage & DB: ${oldImg.imageUrl}")
                        // This image was removed by user
                        if (oldImg.imageUrl.contains("supabase")) {
                            repository.deleteImage(oldImg.imageUrl, "tanaman")
                        }
                        repository.deleteTanamanImageById(currentTanamanId, oldImg.id)
                    }
                }

                // B. Upload new photos
                currentPhotos.forEach { uri ->
                    if (uri.toString().startsWith("content://")) {
                        val url = repository.uploadImage(uri, "tanaman")
                        if (url != null) {
                            val imgEntity = TanamanImageEntity(
                                id = UUID.randomUUID().toString(),
                                tanamanId = currentTanamanId,
                                imageUrl = url
                            )
                            repository.insertTanamanImage(imgEntity)
                        }
                    }
                }
                
                // 3. Log Activity
                AdminLogger.log(
                    appContext,
                    repository,
                    if (tanamanId == null) "Tambah" else "Ubah",
                    "Tanaman",
                    nama,
                    currentTanamanId
                )
            }
            
            com.project.topakuli.util.DialogHelper.showSuccessDialog(
                requireContext(),
                "Berhasil Disimpan",
                "Data tanaman $nama telah berhasil diperbarui di database.",
                "Kembali ke Daftar"
            ) {
                findNavController().navigateUp()
            }
        }
    }

    private fun showMultiSelectResep() {
        if (listResep.isEmpty()) {
            Toast.makeText(requireContext(), "Belum ada resep di database", Toast.LENGTH_SHORT).show()
            return
        }

        val titles = listResep.map { it.judul }.toTypedArray()
        val checkedItems = BooleanArray(listResep.size) { i ->
            selectedResepIds.contains(listResep[i].id)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Resep Terkait")
            .setMultiChoiceItems(titles, checkedItems) { _, which, isChecked ->
                val id = listResep[which].id
                if (isChecked) {
                    if (!selectedResepIds.contains(id)) selectedResepIds.add(id)
                } else {
                    selectedResepIds.remove(id)
                }
            }
            .setPositiveButton("Selesai") { _, _ ->
                updateChips()
            }
            .setNegativeButton("Batal", null)
            .show()
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
            deleteTanaman()
        }

        dialog.show()
    }

    private fun deleteTanaman() {
        val id = tanamanId ?: return
        val nama = binding.etNama.text.toString()
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val repo = (requireActivity().application as TopakuliApplication).repository
                repo.deleteTanaman(id)
                AdminLogger.log(appContext, repo, "Hapus", "Tanaman", nama)
            }
            
            com.project.topakuli.util.DialogHelper.showSuccessDialog(
                requireContext(),
                "Berhasil Dihapus",
                "Data tanaman $nama telah dihapus secara permanen.",
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
