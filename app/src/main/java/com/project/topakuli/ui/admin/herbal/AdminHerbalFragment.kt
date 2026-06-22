package com.project.topakuli.ui.admin.herbal

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
import androidx.recyclerview.widget.GridLayoutManager
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.databinding.FragmentAdminHerbalBinding
import com.project.topakuli.ui.admin.beranda.AdminDashboardViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminHerbalFragment : Fragment() {

    private var _binding: FragmentAdminHerbalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminDashboardViewModel by viewModels {
        AdminDashboardViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    private lateinit var adapter: ManageTanamanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHerbalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupActions()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ManageTanamanAdapter(
            onEditClick = { tanaman ->
                val bundle = Bundle().apply {
                    putString("EXTRA_TANAMAN_ID", tanaman.id)
                }
                findNavController().navigate(R.id.admin_add_tanaman, bundle)
            },
            onItemClick = { tanaman ->
                val bundle = Bundle().apply {
                    putString("EXTRA_TANAMAN_ID", tanaman.id)
                }
                findNavController().navigate(R.id.admin_detail_tanaman, bundle)
            }
        )
        binding.rvTanaman.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = this@AdminHerbalFragment.adapter
        }
    }

    private fun setupActions() {
        binding.fabAddTanaman.setOnClickListener {
            findNavController().navigate(R.id.admin_add_tanaman)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchTanaman(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tanamanPaging.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
