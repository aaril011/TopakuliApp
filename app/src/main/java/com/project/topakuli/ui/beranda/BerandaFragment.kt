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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.topakuli.R
import com.project.topakuli.data.DataDummy
import com.project.topakuli.databinding.FragmentBerandaBinding
import com.project.topakuli.util.FuzzySearchHelper

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchAdapter: SearchResultAdapter

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
        setupSearch()
        setupNavigation()
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
        // Scan Action
        binding.actionScan.apply {
            tvActionTitle.text = "Identifikasi"
            ivActionIcon.setImageResource(R.drawable.scan)
            cardAction.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_accent_green))
            root.setOnClickListener {
                navigateToTab(R.id.navigation_identifikasi)
            }
        }

        // Katalog Action
        binding.actionHerbal.apply {
            tvActionTitle.text = "Katalog"
            ivActionIcon.setImageResource(R.drawable.herbal)
            cardAction.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_accent_blue))
            root.setOnClickListener {
                navigateToTab(R.id.navigation_herbal)
            }
        }

        // Resep Action
        binding.actionResep.apply {
            tvActionTitle.text = "Resep"
            ivActionIcon.setImageResource(R.drawable.ic_ingredients)
            cardAction.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_accent_orange))
            root.setOnClickListener {
                navigateToTab(R.id.navigation_resep)
            }
        }

        // Edu Action
        binding.actionEdu.apply {
            tvActionTitle.text = "Tips Aman"
            ivActionIcon.setImageResource(R.drawable.ic_security)
            cardAction.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg_accent_teal))
            root.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_beranda_to_tipsAmanFragment)
            }
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
                if (query.isNotEmpty()) {
                    performFuzzySearch(query)
                } else {
                    hideSearchResults()
                }
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
        val allTanaman = DataDummy.generateDummyTanaman()
        val allResep = DataDummy.generateDummyResep()

        val results = mutableListOf<SearchResultAdapter.SearchItem>()

        // Cari di Tanaman (Nama, Nama Daerah, Khasiat)
        allTanaman.forEach { tanaman ->
            if (FuzzySearchHelper.isSimilar(query, tanaman.nama) ||
                FuzzySearchHelper.isSimilar(query, tanaman.namaDaerah) ||
                FuzzySearchHelper.isSimilar(query, tanaman.khasiat)
            ) {
                results.add(SearchResultAdapter.SearchItem(
                    tanaman.id, tanaman.nama, tanaman.namaDaerah, tanaman.imageUrl, "Tanaman"
                ))
            }
        }

        // Cari di Resep (Judul, Manfaat, Deskripsi)
        allResep.forEach { resep ->
            if (FuzzySearchHelper.isSimilar(query, resep.judul) ||
                FuzzySearchHelper.isSimilar(query, resep.manfaat) ||
                FuzzySearchHelper.isSimilar(query, resep.deskripsi)
            ) {
                results.add(SearchResultAdapter.SearchItem(
                    resep.id, resep.judul, resep.kategori, resep.imageUrl, "Resep"
                ))
            }
        }

        if (results.isNotEmpty()) {
            showSearchResults()
            searchAdapter.submitList(results)
            binding.tvSearchQueryTitle.text = "Hasil pencarian untuk \"$query\""
        } else {
            // Jika tidak ada hasil, tetap tampilkan container tapi dengan pesan kosong atau sembunyikan
            showSearchResults()
            searchAdapter.submitList(emptyList())
            binding.tvSearchQueryTitle.text = "Tidak ditemukan hasil untuk \"$query\""
        }
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
        val tanamanAdapter = TanamanHomeAdapter { tanaman ->
            val bundle = Bundle().apply {
                putString("tanamanId", tanaman.id)
            }
            findNavController().navigate(R.id.action_navigation_beranda_to_detailTanamanFragment, bundle)
        }

        binding.rvTanamanHariIni.adapter = tanamanAdapter
        binding.rvTanamanHariIni.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val randomTanaman = DataDummy.generateDummyTanaman().shuffled().take(5)
        tanamanAdapter.submitList(randomTanaman)
    }

    private fun setupResepPopuler() {
        val resepAdapter = ResepHomeHorizontalAdapter { resep ->
            val bundle = Bundle().apply {
                putString("resepId", resep.id)
            }
            findNavController().navigate(R.id.action_navigation_beranda_to_detailResepFragment, bundle)
        }

        binding.rvResepPopuler.adapter = resepAdapter
        binding.rvResepPopuler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val resepList = DataDummy.generateDummyResep().shuffled().take(5)
        resepAdapter.submitList(resepList)
    }

    private fun setupNavigation() {
        binding.btnLihatSemuaTanaman.setOnClickListener {
            navigateToTab(R.id.navigation_herbal)
        }

        binding.btnLihatSemuaResep.setOnClickListener {
            navigateToTab(R.id.navigation_resep)
        }

        binding.btnLihatSemuaRiwayat.setOnClickListener {
            navigateToTab(R.id.navigation_identifikasi)
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