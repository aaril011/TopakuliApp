package com.project.topakuli.ui.admin.resep

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
import com.project.topakuli.databinding.FragmentAdminResepBinding
import com.project.topakuli.ui.admin.beranda.AdminDashboardViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminResepFragment : Fragment() {

    private var _binding: FragmentAdminResepBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminDashboardViewModel by viewModels {
        AdminDashboardViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    private lateinit var adapter: ManageResepAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminResepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupActions()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ManageResepAdapter(
            onEditClick = { resep ->
                val bundle = Bundle().apply {
                    putString("EXTRA_RESEP_ID", resep.id)
                }
                findNavController().navigate(R.id.admin_add_resep, bundle)
            },
            onItemClick = { resep ->
                val bundle = Bundle().apply {
                    putString("EXTRA_RESEP_ID", resep.id)
                }
                findNavController().navigate(R.id.admin_detail_resep, bundle)
            }
        )
        binding.rvResep.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = this@AdminResepFragment.adapter
        }
    }

    private fun setupActions() {
        binding.fabAddResep.setOnClickListener {
            findNavController().navigate(R.id.admin_add_resep)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchResep(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resepPaging.collectLatest { pagingData ->
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
