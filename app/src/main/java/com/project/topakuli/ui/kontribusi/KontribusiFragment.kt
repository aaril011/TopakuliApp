package com.project.topakuli.ui.kontribusi

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.remote.model.ContributionForm
import com.project.topakuli.databinding.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.activity.result.contract.ActivityResultContracts
import com.project.topakuli.data.remote.SupabaseStorageService
import android.net.Uri
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class KontribusiFragment : Fragment() {

    private var _binding: FragmentKontribusiBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var userName: String = ""
    private var userPhone: String = ""
    private var userEmail: String = ""
    private var userRole: String = ""

    private var selectedTargetItem: KontribusiAdapter.SearchItem? = null
    private var isAccessingFromMenu: Boolean = false

    private var selectedRecipeImageUri: Uri? = null
    private var selectedPlantImageUri: Uri? = null

    private val pickRecipeImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedRecipeImageUri = it
            val container = binding.containerKontribusi
            val ivPreview = container.findViewById<ImageView>(R.id.iv_preview_resep)
            val layoutPlaceholder = container.findViewById<View>(R.id.layout_placeholder_resep)
            
            ivPreview?.let { iv ->
                iv.visibility = View.VISIBLE
                Glide.with(this).load(it).into(iv)
                layoutPlaceholder?.visibility = View.GONE
            }
        }
    }

    private val pickPlantImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedPlantImageUri = it
            val container = binding.containerKontribusi
            val ivPreview = container.findViewById<ImageView>(R.id.iv_preview_tanaman)
            val layoutPlaceholder = container.findViewById<View>(R.id.layout_placeholder_tanaman)

            ivPreview?.let { iv ->
                iv.visibility = View.VISIBLE
                Glide.with(this).load(it).into(iv)
                layoutPlaceholder?.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKontribusiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            val currentView = if (binding.containerKontribusi.childCount > 0) binding.containerKontribusi.getChildAt(0) else null
            
            // Jika sedang di menu utama atau form data diri (ketika data kosong), baru back beneran (ke beranda)
            // Cek berdasarkan ID root layout yang sedang aktif
            when (currentView?.id) {
                R.id.root_layout_kontribusi_menu -> findNavController().navigateUp()
                R.id.root_layout_kontribusi_data_diri -> {
                    if (isAccessingFromMenu) {
                        showMenuContribution()
                    } else {
                        findNavController().navigateUp()
                    }
                }
                R.id.root_layout_kontribusi_koreksi -> showMenuContribution()
                // Jika di form resep, tanaman, atau koreksi, kembali ke menu
                else -> {
                    showMenuContribution()
                }
            }
        }
        loadUserIdentity()
        
        if (userName.isEmpty() || userPhone.isEmpty() || userEmail.isEmpty()) {
            showIdentityForm(fromMenu = false)
        } else {
            showMenuContribution()
        }
    }

    private fun loadUserIdentity() {
        val prefs = requireContext().getSharedPreferences("TopakuliPrefs", Context.MODE_PRIVATE)
        userName = prefs.getString("userName", "") ?: ""
        userPhone = prefs.getString("userPhone", "") ?: ""
        userEmail = prefs.getString("userEmail", "") ?: ""
        userRole = prefs.getString("userRole", "Warga biasa") ?: "Warga biasa"
    }

    private fun saveUserIdentity(name: String, phone: String, email: String, role: String) {
        requireContext().getSharedPreferences("TopakuliPrefs", Context.MODE_PRIVATE).edit().apply {
            putString("userName", name)
            putString("userPhone", phone)
            putString("userEmail", email)
            putString("userRole", role)
            apply()
        }
        userName = name; userPhone = phone; userEmail = email; userRole = role
    }

    private fun showIdentityForm(fromMenu: Boolean = false) {
        isAccessingFromMenu = fromMenu
        val formBinding = LayoutKontribusiDataDiriBinding.inflate(layoutInflater, binding.containerKontribusi, false)
        binding.containerKontribusi.removeAllViews()
        binding.containerKontribusi.addView(formBinding.root)
        binding.tvTitle.text = "Data Diri"
        
        formBinding.etNamaLengkap.setText(userName)
        formBinding.etNomorTelepon.setText(userPhone)
        formBinding.etEmail.setText(userEmail)

        // Set content for selection items
        formBinding.tvRoleWarga.text = getFormattedRoleText("Warga biasa", "Masyarakat umum yang ingin berbagi pengetahuan")
        formBinding.tvRoleTokoh.text = getFormattedRoleText("Tokoh adat / sesepuh", "Pemegang pengetahuan tradisional turun-temurun")
        formBinding.tvRoleTabib.text = getFormattedRoleText("Dukun / tabib / jamu", "Praktisi pengobatan tradisional")
        formBinding.tvRolePetani.text = getFormattedRoleText("Petani / pekebun", "Mengenal tanaman dari pengalaman bertanam")
        formBinding.tvRolePengunjung.text = getFormattedRoleText("Pengunjung desa pakuli", "Wisatawan atau pendatang yang ingin mempelajari tanaman obat lokal.")

        var currentSelectedRole = userRole
        val roleViews = listOf(
            formBinding.tvRoleWarga to "Warga biasa",
            formBinding.tvRoleTokoh to "Tokoh adat / sesepuh",
            formBinding.tvRoleTabib to "Dukun / tabib / jamu",
            formBinding.tvRolePetani to "Petani / pekebun",
            formBinding.tvRolePengunjung to "Pengunjung desa pakuli"
        )

        fun updateRoleSelectionUI(selected: String) {
            currentSelectedRole = selected
            roleViews.forEach { (view, role) ->
                view.isSelected = (role == selected)
            }
        }

        // Initialize selection
        updateRoleSelectionUI(currentSelectedRole)

        // Set click listeners for selection
        roleViews.forEach { (view, role) ->
            view.setOnClickListener { updateRoleSelectionUI(role) }
        }

        formBinding.btnSimpanIdentitas.setOnClickListener {
            val name = formBinding.etNamaLengkap.text.toString().trim()
            val phone = formBinding.etNomorTelepon.text.toString().trim()
            val email = formBinding.etEmail.text.toString().trim()
            
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveUserIdentity(name, phone, email, currentSelectedRole)
            showMenuContribution()
        }
    }

    private fun getFormattedRoleText(title: String, desc: String): SpannableStringBuilder {
        val ssb = SpannableStringBuilder("$title\n$desc")
        ssb.setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.black)), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(android.text.style.AbsoluteSizeSpan(14, true), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        
        ssb.setSpan(ForegroundColorSpan(android.graphics.Color.parseColor("#6B7280")), title.length + 1, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(android.text.style.AbsoluteSizeSpan(11, true), title.length + 1, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ssb
    }

    private fun showMenuContribution() {
        val menuBinding = LayoutKontribusiMenuBinding.inflate(layoutInflater, binding.containerKontribusi, false)
        binding.containerKontribusi.removeAllViews()
        binding.containerKontribusi.addView(menuBinding.root)
        binding.tvTitle.text = "Saran dan Koreksi"

        menuBinding.tvUserNameSummary.text = userName
        menuBinding.tvUserRoleSummary.text = userRole
        
        val initials = userName.split(" ").filter { it.isNotEmpty() }.take(2)
            .map { it[0].uppercase() }.joinToString("")
        menuBinding.tvUserInitialSummary.text = if (initials.isNotEmpty()) initials else "US"
        
        menuBinding.btnGantiIdentitas.setOnClickListener { showIdentityForm(fromMenu = true) }

        setupMenuItem(menuBinding.menuResepBaru.root, "Ajukan resep baru", "Resep ramuan yang belum ada di aplikasi", R.drawable.icoon_book) { showRecipeForm() }
        setupMenuItem(menuBinding.menuTanamanBaru.root, "Tambah khasiat tanaman", "Manfaat tanaman yang belum tercatat", R.drawable.herbal) { showPlantForm() }
        setupMenuItem(menuBinding.menuKoreksiData.root, "Koreksi data yang salah", "Ada data yang kurang tepat di aplikasi", R.drawable.basil_edit_outline) { showCorrectionForm() }
    }

    private fun setupMenuItem(view: View, title: String, desc: String, iconRes: Int, onClick: () -> Unit) {
        view.findViewById<TextView>(R.id.tv_menu_title).text = title
        view.findViewById<TextView>(R.id.tv_menu_desc).text = desc
        view.findViewById<ImageView>(R.id.iv_menu_icon).setImageResource(iconRes)
        view.setOnClickListener { onClick() }
    }

    private fun showRecipeForm() {
        isAccessingFromMenu = false
        val formBinding = LayoutKontribusiResepFormBinding.inflate(layoutInflater, binding.containerKontribusi, false)
        binding.containerKontribusi.removeAllViews()
        binding.containerKontribusi.addView(formBinding.root)
        binding.tvTitle.text = "Ajukan resep baru"
        
        formBinding.layoutUserCard.tvUserNameMini.text = userName
        formBinding.layoutUserCard.tvUserRoleMini.text = userRole
        formBinding.layoutUserCard.btnGantiUser.setOnClickListener { showIdentityForm(fromMenu = true) }

        formBinding.btnUploadResep.setOnClickListener {
            pickRecipeImage.launch("image/*")
        }

        formBinding.btnKirimResep.setOnClickListener {
            val name = formBinding.etNamaResep.text.toString().trim()
            val keluhan = formBinding.etKeluhan.text.toString().trim()
            val desc = formBinding.etDeskripsiResep.text.toString().trim()
            val bahanText = formBinding.etBahan.text.toString().trim()
            val langkahText = formBinding.etLangkah.text.toString().trim()
            
            val ref = formBinding.etSumberResep.text.toString().trim()
            val prepTime = formBinding.etPrepTime.text.toString().trim()
            val usage = formBinding.etUsageMode.text.toString().trim()
            val frequency = formBinding.etFrequency.text.toString().trim()
            val shelfLife = formBinding.etShelfLife.text.toString().trim()
            val sideEffects = formBinding.etSideEffects.text.toString().trim()

            if (name.isEmpty() || desc.isEmpty() || keluhan.isEmpty() || bahanText.isEmpty() || langkahText.isEmpty() ||
                prepTime.isEmpty() || usage.isEmpty() || frequency.isEmpty() || shelfLife.isEmpty() || sideEffects.isEmpty() || ref.isEmpty() || selectedRecipeImageUri == null) {
                Toast.makeText(requireContext(), "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                val supabase = (requireActivity().application as TopakuliApplication).supabaseStorageService
                val imageUrl = selectedRecipeImageUri?.let { supabase.uploadImage(it, "pengajuan_resep") }
                
                submitContribution(ContributionForm(
                    id = UUID.randomUUID().toString(),
                    userId = auth.currentUser?.uid ?: "anonymous",
                    userName = userName, userEmail = userEmail, userPhone = userPhone, userRole = userRole,
                    type = "resep", 
                    recipeName = name, 
                    recipeSymptoms = keluhan,
                    recipeDescription = desc,
                    recipeIngredientsText = bahanText,
                    recipeStepsText = langkahText,
                    recipePrepTime = prepTime, 
                    recipeUsage = usage,
                    recipeFrequency = frequency,
                    recipeShelfLife = shelfLife,
                    recipeSideEffects = sideEffects,
                    recipeReference = ref,
                    recipeImageUrl = imageUrl
                ))
            }
        }
    }

    private fun addDynamicRow(container: ViewGroup, hintMain: String, hintSub: String?, isIngredient: Boolean) {
        val rowView = layoutInflater.inflate(R.layout.item_form_dynamic_row, container, false)
        val etMain = rowView.findViewById<EditText>(R.id.et_input_main)
        val etSub = rowView.findViewById<EditText>(R.id.et_input_sub)
        val btnRemove = rowView.findViewById<ImageView>(R.id.btn_remove_row)

        etMain.hint = hintMain
        if (hintSub != null) {
            etSub.visibility = View.VISIBLE
            etSub.hint = hintSub
        }
        
        btnRemove.setOnClickListener {
            if (container.childCount > 1) { 
                 container.removeView(rowView)
            }
        }
        
        // Add before the "Add" button
        container.addView(rowView, container.childCount - 1)
    }

    private fun getDynamicRowData(container: ViewGroup): List<Pair<String, String>> {
        val data = mutableListOf<Pair<String, String>>()
        for (i in 0 until container.childCount) {
            val view = container.getChildAt(i)
            val etMain = view.findViewById<EditText>(R.id.et_input_main)
            val etSub = view.findViewById<EditText>(R.id.et_input_sub)
            if (etMain != null) {
                val mainText = etMain.text.toString().trim()
                val subText = etSub.text.toString().trim()
                if (mainText.isNotEmpty()) {
                    data.add(mainText to subText)
                }
            }
        }
        return data
    }

    private fun showPlantForm() {
        isAccessingFromMenu = false
        val formBinding = LayoutKontribusiTanamanFormBinding.inflate(layoutInflater, binding.containerKontribusi, false)
        binding.containerKontribusi.removeAllViews()
        binding.containerKontribusi.addView(formBinding.root)
        binding.tvTitle.text = "Tambah khasiat tanaman"
        
        formBinding.layoutUserCard.tvUserNameMini.text = userName
        formBinding.layoutUserCard.tvUserRoleMini.text = userRole
        formBinding.layoutUserCard.btnGantiUser.setOnClickListener { showIdentityForm(fromMenu = true) }

        formBinding.btnUploadGambar.setOnClickListener {
            pickPlantImage.launch("image/*")
        }

        formBinding.btnKirimTanaman.setOnClickListener {
            val name = formBinding.etNamaTanaman.text.toString().trim()
            val localName = formBinding.etNamaDaerah.text.toString().trim()
            val latin = formBinding.etNamaLatin.text.toString().trim()
            val benefits = formBinding.etKhasiatTanaman.text.toString().trim()
            val desc = formBinding.etDeskripsiTanaman.text.toString().trim()
            val ref = formBinding.etSumberReferensi.text.toString().trim()

            if (name.isEmpty() || localName.isEmpty() || latin.isEmpty() || benefits.isEmpty() || desc.isEmpty() || ref.isEmpty() || selectedPlantImageUri == null) {
                Toast.makeText(requireContext(), "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                val supabase = (requireActivity().application as TopakuliApplication).supabaseStorageService
                val imageUrl = selectedPlantImageUri?.let { supabase.uploadImage(it, "pengajuan_tanamanoabat") }

                submitContribution(ContributionForm(
                    id = UUID.randomUUID().toString(),
                    userId = auth.currentUser?.uid ?: "anonymous",
                    userName = userName, userEmail = userEmail, userPhone = userPhone, userRole = userRole,
                    type = "tanaman", 
                    plantName = name, 
                    plantLocalName = localName,
                    plantLatin = latin,
                    plantBenefits = benefits, 
                    plantDescription = desc,
                    plantReference = ref,
                    plantImageUrl = imageUrl
                ))
            }
        }
    }

    private fun showCorrectionForm() {
        isAccessingFromMenu = false
        val formBinding = LayoutKontribusiKoreksiFormBinding.inflate(layoutInflater, binding.containerKontribusi, false)
        binding.containerKontribusi.removeAllViews()
        binding.containerKontribusi.addView(formBinding.root)
        binding.tvTitle.text = "Koreksi data"
        
        formBinding.layoutUserCard.tvUserNameMini.text = userName
        formBinding.layoutUserCard.tvUserRoleMini.text = userRole
        formBinding.layoutUserCard.btnGantiUser.setOnClickListener { showIdentityForm(fromMenu = true) }

        val searchAdapter = KontribusiAdapter.SearchResultAdapter { selected ->
            selectedTargetItem = selected
            formBinding.tvSelectedName.text = selected.name
            formBinding.cardSelectedItem.visibility = View.VISIBLE
            formBinding.rvSearchResultsKoreksi.visibility = View.GONE
            formBinding.etSearchKoreksi.setText("")
        }

        formBinding.rvSearchResultsKoreksi.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        formBinding.etSearchKoreksi.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = formBinding.etSearchKoreksi.text.toString().trim()
                if (query.length >= 2) {
                    performDataSearch(query) { results ->
                        searchAdapter.submitList(results)
                        formBinding.rvSearchResultsKoreksi.visibility = if (results.isNotEmpty()) View.VISIBLE else View.GONE
                        formBinding.tvNoResults.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                true
            } else false
        }

        formBinding.etSearchKoreksi.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    searchAdapter.submitList(emptyList())
                    formBinding.rvSearchResultsKoreksi.visibility = View.GONE
                    formBinding.tvNoResults.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        formBinding.btnRemoveSelection.setOnClickListener {
            selectedTargetItem = null
            formBinding.cardSelectedItem.visibility = View.GONE
        }

        formBinding.btnKirimKoreksi.setOnClickListener {
            val part = formBinding.etBagianKoreksi.text.toString().trim()
            val note = formBinding.etPenjelasanKoreksi.text.toString().trim()
            val reference = formBinding.etSumberKoreksi.text.toString().trim()
            
            if (selectedTargetItem == null || part.isEmpty() || note.isEmpty() || reference.isEmpty()) {
                Toast.makeText(requireContext(), "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            submitContribution(ContributionForm(
                id = UUID.randomUUID().toString(),
                userId = auth.currentUser?.uid ?: "anonymous",
                userName = userName, userEmail = userEmail, userPhone = userPhone, userRole = userRole,
                type = "koreksi", targetItemId = selectedTargetItem?.id, targetItemName = selectedTargetItem?.name,
                targetItemType = selectedTargetItem?.type, correctionPart = part, correctionNote = note,
                correctionReference = reference
            ))
        }
    }

    private fun performDataSearch(query: String, callback: (List<KontribusiAdapter.SearchItem>) -> Unit) {
        lifecycleScope.launch {
            val repo = (requireActivity().application as TopakuliApplication).repository
            val plants = repo.allTanaman.first().filter { it.nama.contains(query, true) }
                .map { KontribusiAdapter.SearchItem(it.id, it.nama, it.namaLatin, "tanaman") }
            val recipes = repo.allResep.first().filter { it.judul.contains(query, true) }
                .map { KontribusiAdapter.SearchItem(it.id, it.judul, it.kategori, "resep") }
            callback(plants + recipes)
        }
    }

    private fun submitContribution(form: ContributionForm) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Gunakan satu collection utama: user_contributions
                firestore.collection("user_contributions").document(form.id).set(form).await()
                
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    showSuccessDialog()
                }
            } catch (e: Exception) {
                if (isAdded) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Gagal mengirim: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_success_contribution, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<View>(R.id.btn_close_success).setOnClickListener {
            dialog.dismiss()
            showMenuContribution()
        }
        
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
