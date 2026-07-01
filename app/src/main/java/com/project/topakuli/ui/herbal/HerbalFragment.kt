package com.project.topakuli.ui.herbal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.databinding.FragmentHerbalBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HerbalFragment : Fragment() {

    private var _binding: FragmentHerbalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HerbalViewModel by viewModels {
        HerbalViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    private lateinit var adapter: TanamanAdapter
    private var isGridView = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHerbalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Reset total setiap kali fragment dibuat/masuk kembali
        viewModel.resetState()
        binding.tabLayout.getTabAt(0)?.select()
        binding.etSearch.setText("")
        
        setupRecyclerView()
        setupSearch()
        setupTabs()
        setupLayoutToggle()
        observeViewModel()
        setupEmptyState()
    }

    private fun setupLayoutToggle() {
        binding.btnChangeLayout.setOnClickListener {
            isGridView = !isGridView
            updateLayout()
        }
    }

    private fun updateLayout() {
        if (isGridView) {
            binding.rvTanaman.layoutManager = GridLayoutManager(requireContext(), 3)
            binding.btnChangeLayout.setImageResource(R.drawable.ic_list_view)
        } else {
            binding.rvTanaman.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            binding.btnChangeLayout.setImageResource(R.drawable.ic_grid_view)
        }
        adapter.setViewType(isGridView)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tanamanPaging.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TanamanAdapter(
            onItemClick = { tanaman ->
                val bundle = Bundle().apply { putString("tanamanId", tanaman.id) }
                findNavController().navigate(R.id.action_navigation_herbal_to_detailTanamanFragment, bundle)
            },
            onFavoriteClick = { tanaman ->
                viewModel.toggleFavorit(tanaman)
            }
        )
        updateLayout()
        binding.rvTanaman.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.setOnlyFavorites(tab?.position == 1)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupEmptyState() {
        binding.emptyState.btnRetry.setOnClickListener {
            viewModel.syncData()
            adapter.retry()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                val isListEmpty = loadStates.refresh is LoadState.NotLoading && adapter.itemCount == 0
                val isOffline = loadStates.refresh is LoadState.Error && adapter.itemCount == 0
                
                binding.emptyState.root.visibility = if (isListEmpty || isOffline) View.VISIBLE else View.GONE
                
                if (isOffline) {
                    binding.emptyState.ivEmptyIllustration.setImageResource(R.drawable.ic_no_connection)
                    binding.emptyState.tvEmptyTitle.text = "Mode Offline"
                    binding.emptyState.tvEmptyDesc.text = "Data belum tersedia di perangkat. Aktifkan internet untuk sinkronisasi pertama kali."
                    binding.emptyState.btnRetry.visibility = View.VISIBLE
                } else if (isListEmpty) {
                    binding.emptyState.btnRetry.visibility = View.GONE
                    val query = viewModel.searchQuery.value
                    val onlyFav = viewModel.onlyFavorites.value

                    if (query.isNotEmpty()) {
                        binding.emptyState.ivEmptyIllustration.setImageResource(R.drawable.ic_empty_search)
                        binding.emptyState.tvEmptyTitle.text = "Tanaman tidak ditemukan"
                        binding.emptyState.tvEmptyDesc.text = "Coba sesuaikan kata kunci pencarian Anda"
                    } else if (onlyFav) {
                        binding.emptyState.ivEmptyIllustration.setImageResource(R.drawable.ic_empty_favorites)
                        binding.emptyState.tvEmptyTitle.text = "Tanaman favorit masih kosong"
                        binding.emptyState.tvEmptyDesc.text = "Belum ada tanaman yang Anda tandai sebagai favorit"
                    } else {
                        binding.emptyState.ivEmptyIllustration.setImageResource(R.drawable.ic_no_connection)
                        binding.emptyState.tvEmptyTitle.text = "Data Kosong"
                        binding.emptyState.tvEmptyDesc.text = "Tidak ada data tanaman obat saat ini"
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Reset setiap kali fragment kembali aktif (misal dari detail atau navigasi bottom nav)
        viewModel.resetState()
        binding.tabLayout.getTabAt(0)?.select()
        binding.etSearch.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
