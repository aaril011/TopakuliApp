package com.project.topakuli.ui.beranda

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.databinding.FragmentBerandaBinding
import com.project.topakuli.ui.history.HistoryAdapter
import com.project.topakuli.ui.notifikasi.NotifikasiActivity
import com.project.topakuli.util.FuzzySearchHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BerandaViewModel by viewModels {
        BerandaViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    private lateinit var searchAdapter: SearchResultAdapter
    private lateinit var tanamanAdapter: TanamanHomeAdapter
    private lateinit var resepAdapter: ResepHomeHorizontalAdapter
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGreeting()
        setupQuickActions()
        setupTanamanHariIni()
        setupResepPopuler()
        setupHistory()
        setupSearch()
        setupNavigation()
        observeViewModel()
        
        checkFirstRun()
    }

    private fun checkFirstRun() {
        val prefs = requireContext().getSharedPreferences("TopakuliPrefs", android.content.Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("isFirstRun", true)
        
        if (isFirstRun) {
            viewLifecycleOwner.lifecycleScope.launch {
                // Wait for initial data to load (shimmer to disappear)
                combine(viewModel.randomTanaman, viewModel.popularResep) { tanaman, resep ->
                    tanaman.isNotEmpty() && resep.isNotEmpty()
                }.filter { it }.first()
                
                kotlinx.coroutines.delay(800) // Professional pause after content is ready
                if (isAdded) showOriginSelectionDialog(prefs)
            }
        }
    }

    private fun showOriginSelectionDialog(prefs: android.content.SharedPreferences) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_visitor_origin, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setWindowAnimations(R.style.DialogAnimation)
        }

        val btnPakuli = dialogView.findViewById<View>(R.id.btn_origin_pakuli)
        val btnOutside = dialogView.findViewById<View>(R.id.btn_origin_outside)

        btnPakuli.setOnClickListener {
            viewModel.submitVisitorOrigin("Desa Pakuli")
            prefs.edit().putBoolean("isFirstRun", false).apply()
            dialog.dismiss()
        }

        btnOutside.setOnClickListener {
            viewModel.submitVisitorOrigin("Luar Desa Pakuli")
            prefs.edit().putBoolean("isFirstRun", false).apply()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        // Reset pencarian setiap kali Beranda tampil kembali
        binding.etSearch.setText("")
        hideSearchResults()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.randomTanaman.collectLatest { list ->
                        if (list.isNotEmpty()) {
                            binding.shimmerTanaman.stopShimmer()
                            binding.shimmerTanaman.visibility = View.GONE
                            binding.rvTanamanHariIni.visibility = View.VISIBLE
                            tanamanAdapter.submitList(list) {
                                binding.rvTanamanHariIni.scrollToPosition(0)
                            }
                        }
                    }
                }
                launch {
                    viewModel.popularResep.collectLatest { list ->
                        if (list.isNotEmpty()) {
                            binding.shimmerResep.stopShimmer()
                            binding.shimmerResep.visibility = View.GONE
                            binding.rvResepPopuler.visibility = View.VISIBLE
                            resepAdapter.submitList(list) {
                                binding.rvResepPopuler.scrollToPosition(0)
                            }
                        }
                    }
                }
                launch {
                    viewModel.recentHistory.collectLatest { list ->
                        val hasHistory = list.isNotEmpty()
                        binding.rvHistoryHome.visibility = if (hasHistory) View.VISIBLE else View.GONE
                        binding.layoutEmptyHistory.visibility = if (hasHistory) View.GONE else View.VISIBLE
                        if (hasHistory) {
                            historyAdapter.submitList(list)
                        }
                    }
                }
            }
        }
    }

    private fun setupGreeting() {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 4..10 -> "Selamat Pagi,"
            in 11..14 -> "Selamat Siang,"
            in 15..18 -> "Selamat Sore,"
            else -> "Selamat Malam,"
        }
        binding.tvGreeting.text = greeting
    }

    private fun setupQuickActions() {
        binding.actionScan.apply {
            tvActionTitle.text = "Identifikasi"
            ivActionIcon.setImageResource(R.drawable.scan)
            cardAction.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_accent_green))
            root.setOnClickListener { navigateToTab(R.id.navigation_identifikasi) }
        }

        binding.actionHerbal.apply {
            tvActionTitle.text = "Katalog"
            ivActionIcon.setImageResource(R.drawable.herbal)
            cardAction.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_accent_blue))
            root.setOnClickListener { navigateToTab(R.id.navigation_herbal) }
        }

        binding.actionResep.apply {
            tvActionTitle.text = "Resep"
            ivActionIcon.setImageResource(R.drawable.ic_ingredients)
            cardAction.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_accent_orange))
            root.setOnClickListener { navigateToTab(R.id.navigation_resep) }
        }

        binding.actionEdu.apply {
            tvActionTitle.text = "Tips Aman"
            ivActionIcon.setImageResource(R.drawable.ic_security)
            cardAction.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_accent_teal))
            root.setOnClickListener { findNavController().navigate(R.id.action_navigation_beranda_to_tipsAmanFragment) }
        }
    }

    private fun setupSearch() {
        searchAdapter = SearchResultAdapter { id, type ->
            val bundle = Bundle().apply {
                if (type == "Tanaman") putString("tanamanId", id)
                else putString("resepId", id)
            }
            val destination = if (type == "Tanaman") R.id.action_navigation_beranda_to_detailTanamanFragment
            else R.id.action_navigation_beranda_to_detailResepFragment
            findNavController().navigate(destination, bundle)
        }

        binding.rvSearchResults.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) performFuzzySearch(query) else hideSearchResults()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performFuzzySearch(binding.etSearch.text.toString())
                true
            } else false
        }
    }

    private fun performFuzzySearch(query: String) {
        val allTanaman = viewModel.allTanaman.value
        val allResep = viewModel.allResep.value
        val results = mutableListOf<SearchResultAdapter.SearchItem>()

        allTanaman.forEach { tanaman ->
            if (FuzzySearchHelper.isSimilar(query, tanaman.nama) ||
                FuzzySearchHelper.isSimilar(query, tanaman.namaDaerah) ||
                FuzzySearchHelper.isSimilar(query, tanaman.khasiat)
            ) {
                results.add(SearchResultAdapter.SearchItem(tanaman.id, tanaman.nama, tanaman.namaDaerah, tanaman.imageUrl, "Tanaman"))
            }
        }

        allResep.forEach { resep ->
            if (FuzzySearchHelper.isSimilar(query, resep.judul) ||
                FuzzySearchHelper.isSimilar(query, resep.manfaat) ||
                FuzzySearchHelper.isSimilar(query, resep.deskripsi)
            ) {
                results.add(SearchResultAdapter.SearchItem(resep.id, resep.judul, resep.kategori, resep.imageUrl, "Resep"))
            }
        }

        showSearchResults()
        searchAdapter.submitList(results)
        binding.tvSearchQueryTitle.text = if (results.isNotEmpty()) "Hasil pencarian untuk \"$query\"" else "Tidak ditemukan hasil untuk \"$query\""
    }

    private fun showSearchResults() {
        binding.mainContent.visibility = View.GONE
        binding.searchResultsContainer.visibility = View.VISIBLE
    }

    private fun hideSearchResults() {
        binding.mainContent.visibility = View.VISIBLE
        binding.searchResultsContainer.visibility = View.GONE
    }

    private fun setupTanamanHariIni() {
        tanamanAdapter = TanamanHomeAdapter { tanaman ->
            val bundle = Bundle().apply { putString("tanamanId", tanaman.id) }
            findNavController().navigate(R.id.action_navigation_beranda_to_detailTanamanFragment, bundle)
        }
        binding.rvTanamanHariIni.adapter = tanamanAdapter
        binding.rvTanamanHariIni.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupResepPopuler() {
        resepAdapter = ResepHomeHorizontalAdapter { resep ->
            val bundle = Bundle().apply { putString("resepId", resep.id) }
            findNavController().navigate(R.id.action_navigation_beranda_to_detailResepFragment, bundle)
        }
        binding.rvResepPopuler.adapter = resepAdapter
        binding.rvResepPopuler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupHistory() {
        historyAdapter = HistoryAdapter(
            onSelectionChanged = { /* Beranda tidak butuh selection mode */ },
            onItemClick = { history ->
                val bundle = Bundle().apply {
                    putInt("historyId", history.id)
                    putString("tanamanId", history.tanamanId)
                    putString("nama", history.namaTanaman)
                    putString("namaLatin", history.namaLatin)
                    putString("namaDaerah", history.namaDaerah)
                    putFloat("akurasi", history.akurasi.toFloat())
                    putLong("timestamp", history.timestamp)
                    putString("imagePath", history.imagePath)
                }
                findNavController().navigate(R.id.action_navigation_beranda_to_historyDetailFragment, bundle)
            }
        )
        binding.rvHistoryHome.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun setupNavigation() {
        binding.ivNotifikasi.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), NotifikasiActivity::class.java))
        }
        binding.btnLihatSemuaTanaman.setOnClickListener { navigateToTab(R.id.navigation_herbal) }
        binding.btnLihatSemuaResep.setOnClickListener { navigateToTab(R.id.navigation_resep) }
        binding.btnLihatSemuaRiwayat.setOnClickListener { 
            findNavController().navigate(R.id.action_navigation_beranda_to_historyFragment)
        }
        binding.btnKontribusi.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_beranda_to_kontribusi)
        }
    }

    private fun navigateToTab(destinationId: Int) {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNav.selectedItemId = destinationId
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
