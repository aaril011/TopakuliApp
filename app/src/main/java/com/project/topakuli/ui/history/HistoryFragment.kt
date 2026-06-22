package com.project.topakuli.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupActions()
        setupBackNavigation()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onSelectionChanged = { count ->
                updateSelectionUi(count)
            },
            onItemClick = { history ->
                val bundle = Bundle().apply {
                    putInt("historyId", history.id)
                    putString("tanamanId", history.tanamanId)
                    putString("nama", history.namaTanaman)
                    putFloat("akurasi", history.akurasi.toFloat())
                    putLong("timestamp", history.timestamp)
                    putString("imagePath", history.imagePath)
                }
                findNavController().navigate(R.id.action_historyFragment_to_historyDetailFragment, bundle)
            }
        )
        binding.rvHistory.apply {
            adapter = this@HistoryFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.allHistory.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val isEmpty = list.isEmpty()
            binding.rvHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.emptyStateHistory.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.btnMore.visibility = if (isEmpty) View.GONE else View.VISIBLE

            if (isEmpty) {
                binding.emptyStateHistory.tvEmptyTitle.text = "Riwayat Kosong"
                binding.emptyStateHistory.tvEmptyDesc.text = "Belum ada riwayat identifikasi. Mulai identifikasi tanaman Anda sekarang!"
                binding.emptyStateHistory.ivEmptyIllustration.setImageResource(R.drawable.scan)
                binding.emptyStateHistory.btnRetry.visibility = View.GONE
            }
        }
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener {
            if (adapter.isSelectionMode()) {
                exitSelectionMode()
            } else {
                findNavController().navigateUp()
            }
        }

        binding.btnMore.setOnClickListener {
            showMoreMenu(it)
        }

        binding.btnCancelSelection.setOnClickListener {
            exitSelectionMode()
        }

        binding.btnDeleteSelected.setOnClickListener {
            showDeleteSelectedDialog()
        }
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.isSelectionMode()) {
                    exitSelectionMode()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun showMoreMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.history_more_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_select_data -> {
                    enterSelectionMode()
                    true
                }
                R.id.menu_delete_all -> {
                    showDeleteAllDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun enterSelectionMode() {
        adapter.setSelectionMode(true)
        binding.btnMore.visibility = View.GONE
        binding.tvTitleHistory.text = "Pilih Data"
    }

    private fun exitSelectionMode() {
        adapter.setSelectionMode(false)
        binding.btnMore.visibility = View.VISIBLE
        binding.tvTitleHistory.text = "Riwayat"
        binding.layoutDeleteActions.visibility = View.GONE
    }

    private fun updateSelectionUi(count: Int) {
        if (adapter.isSelectionMode()) {
            binding.layoutDeleteActions.visibility = if (count > 0) View.VISIBLE else View.GONE
            binding.tvSelectedCount.text = "$count data dipilih"
            binding.btnDeleteSelected.isEnabled = count > 0
        }
    }

    private fun showDeleteSelectedDialog() {
        val selectedIds = adapter.getSelectedIds()
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Data")
            .setMessage("Hapus ${selectedIds.size} data yang dipilih?")
            .setPositiveButton("Hapus") { _, _ ->
                selectedIds.forEach { viewModel.deleteHistory(it) }
                exitSelectionMode()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Semua")
            .setMessage("Apakah Anda yakin ingin menghapus semua riwayat identifikasi?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.clearAllHistory()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
