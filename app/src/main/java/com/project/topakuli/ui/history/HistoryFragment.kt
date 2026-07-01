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
                    putString("namaLatin", history.namaLatin)
                    putString("namaDaerah", history.namaDaerah)
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
            binding.btnMore.visibility = View.VISIBLE // Tetap tampilkan titik 3

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
            val isEmpty = viewModel.allHistory.value.isNullOrEmpty()
            
            when (item.itemId) {
                R.id.menu_select_data -> {
                    if (isEmpty) {
                        showNoDataDialog()
                    } else {
                        enterSelectionMode()
                    }
                    true
                }
                R.id.menu_delete_all -> {
                    if (isEmpty) {
                        showNoDataDialog()
                    } else {
                        showDeleteAllDialog()
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showNoDataDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cardIcon = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_info_icon)
        cardIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#F59E0B")) // Orange
        
        // Find the ImageView inside the CardView
        val imageView = cardIcon.getChildAt(0) as? android.widget.ImageView
        imageView?.setImageResource(R.drawable.info)
        
        val title = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_title)
        val message = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_message)
        val btnCancel = dialogView.findViewById<View>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_confirm_delete)

        title.text = "Peringatan"
        title.setTextColor(android.graphics.Color.parseColor("#92400E"))
        
        // Change header background color to light orange
        val header = title.parent as? View
        header?.setBackgroundColor(android.graphics.Color.parseColor("#FFFBEB"))

        message.text = "Tidak ada data riwayat yang dapat dihapus."
        btnCancel.visibility = View.GONE
        btnConfirm.text = "Tutup"
        btnConfirm.setBackgroundColor(android.graphics.Color.parseColor("#F59E0B"))
        
        // Centering the button
        val params = btnConfirm.layoutParams as android.widget.LinearLayout.LayoutParams
        params.width = android.widget.LinearLayout.LayoutParams.MATCH_PARENT
        params.weight = 0f
        params.marginStart = 0
        btnConfirm.layoutParams = params
        
        btnConfirm.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSuccessDeleteDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_success_contribution, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val title = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_title)
        val message = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_message)
        val btnClose = dialogView.findViewById<View>(R.id.btn_close_success)

        title.text = "Berhasil Dihapus"
        message.text = "Semua riwayat identifikasi telah berhasil dihapus dari perangkat Anda."
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val message = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_message)
        val btnCancel = dialogView.findViewById<android.view.View>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<android.view.View>(R.id.btn_confirm_delete)

        message.text = "Hapus ${selectedIds.size} data riwayat yang dipilih?"
        
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            selectedIds.forEach { viewModel.deleteHistory(it) }
            dialog.dismiss()
            exitSelectionMode()
            showSuccessDeleteDialog()
        }

        dialog.show()
    }

    private fun showDeleteAllDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val message = dialogView.findViewById<android.widget.TextView>(R.id.tv_dialog_message)
        val btnCancel = dialogView.findViewById<android.view.View>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<android.view.View>(R.id.btn_confirm_delete)

        message.text = "Apakah Anda yakin ingin menghapus semua riwayat identifikasi?"
        
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            viewModel.clearAllHistory()
            dialog.dismiss()
            showSuccessDeleteDialog()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
