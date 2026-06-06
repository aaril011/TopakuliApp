package com.project.topakuli.ui.resep

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.project.topakuli.R
import com.project.topakuli.data.DataDummy
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.databinding.FragmentResepBinding

class ResepFragment : Fragment() {

    private var _binding: FragmentResepBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ResepAdapter
    private var allResepList: MutableList<ResepEntity> = DataDummy.generateDummyResep().toMutableList()
    private var currentFilter: String = ""
    private var currentTab: Int = 0 // 0: Semua, 1: Disimpan

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupTabs()

        // Logika toggle search bar untuk resep
        binding.ivSearchResep.setOnClickListener {
            binding.layoutSearchBarResep.visibility = if (binding.layoutSearchBarResep.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ResepAdapter(
            onItemClick = { resep ->
                val bundle = Bundle().apply {
                    putString("resepId", resep.id)
                }
                findNavController().navigate(R.id.action_navigation_resep_to_detailResepFragment, bundle)
            },
            onSaveClick = { resep ->
                val index = allResepList.indexOfFirst { it.id == resep.id }
                if (index != -1) {
                    allResepList[index] = resep.copy(isSaved = !resep.isSaved)
                    updateList()
                }
            }
        )
        binding.rvResep.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResep.adapter = adapter

        updateList()
    }

    private fun setupSearch() {
        binding.etSearchResep.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentFilter = s.toString()
                updateList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTabs() {
        binding.tabLayoutResep.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
            allResepList.filter { it.isSaved }
        } else {
            allResepList
        }

        if (currentFilter.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.judul.contains(currentFilter, ignoreCase = true) ||
                it.deskripsi.contains(currentFilter, ignoreCase = true) ||
                it.kategori.contains(currentFilter, ignoreCase = true)
            }
        }

        adapter.submitList(filteredList.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
