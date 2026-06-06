package com.project.topakuli.ui.herbal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.project.topakuli.R
import com.project.topakuli.data.DataDummy
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.databinding.FragmentHerbalBinding

class HerbalFragment : Fragment() {

    private var _binding: FragmentHerbalBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TanamanAdapter
    private var allTanamanList: MutableList<TanamanObatEntity> = DataDummy.generateDummyTanaman().toMutableList()
    private var currentFilter: String = ""
    private var currentTab: Int = 0 // 0: Semua, 1: Favorit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHerbalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupTabs()

        // Toggle search bar visibility
        binding.ivSearch.setOnClickListener {
            binding.layoutSearchBar.visibility = if (binding.layoutSearchBar.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TanamanAdapter(
            onItemClick = { tanaman ->
                val bundle = Bundle().apply {
                    putString("tanamanId", tanaman.id)
                }
                findNavController().navigate(R.id.action_navigation_herbal_to_detailTanamanFragment, bundle)
            },
            onFavoriteClick = { tanaman ->
                toggleFavorite(tanaman)
            }
        )
        binding.rvTanaman.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvTanaman.adapter = adapter
        
        updateList()
    }

    private fun toggleFavorite(tanaman: TanamanObatEntity) {
        val index = allTanamanList.indexOfFirst { it.id == tanaman.id }
        if (index != -1) {
            val updatedTanaman = allTanamanList[index].copy(isFavorit = !allTanamanList[index].isFavorit)
            allTanamanList[index] = updatedTanaman
            updateList()
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentFilter = s.toString()
                updateList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                updateList()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateList() {
        var filteredList = if (currentTab == 1) {
            allTanamanList.filter { it.isFavorit }
        } else {
            allTanamanList
        }

        if (currentFilter.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.nama.contains(currentFilter, ignoreCase = true) ||
                it.namaDaerah.contains(currentFilter, ignoreCase = true) ||
                it.namaLatin.contains(currentFilter, ignoreCase = true)
            }
        }

        adapter.submitList(filteredList.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
