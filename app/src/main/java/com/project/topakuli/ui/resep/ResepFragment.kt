package com.project.topakuli.ui.resep

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.databinding.FragmentResepBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ResepFragment : Fragment() {

    private var _binding: FragmentResepBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResepViewModel by viewModels {
        ResepViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    private lateinit var adapter: ResepAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Reset total setiap kali fragment dibuat/masuk kembali
        viewModel.resetState()
        
        val showHistory = arguments?.getBoolean("showHistory") ?: false
        if (showHistory) {
            binding.tabLayoutResep.getTabAt(2)?.select()
        } else {
            binding.tabLayoutResep.getTabAt(0)?.select()
        }
        
        binding.etSearchResep.setText("")

        setupRecyclerView()
        setupSearch()
        setupTabs()
        observeViewModel()
        setupEmptyState()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resepPaging.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ResepAdapter(
            onItemClick = { resep ->
                val bundle = Bundle().apply { putString("resepId", resep.id) }
                findNavController().navigate(R.id.action_navigation_resep_to_detailResepFragment, bundle)
            },
            onSaveClick = { resep ->
                viewModel.toggleSave(resep)
            },
            onItemLongClick = { resep ->
                if (viewModel.filterMode.value == ResepViewModel.FilterMode.HISTORY) {
                    showDeleteHistoryDialog(resep)
                }
            }
        )
        binding.rvResep.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResep.adapter = adapter
        
        setupSwipeToDelete()
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (viewModel.filterMode.value == ResepViewModel.FilterMode.HISTORY) {
                    val position = viewHolder.bindingAdapterPosition
                    val item = adapter.peek(position)
                    item?.let { viewModel.removeFromHistory(it) }
                } else {
                    adapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
                }
            }

            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return if (viewModel.filterMode.value == ResepViewModel.FilterMode.HISTORY) super.getSwipeDirs(recyclerView, viewHolder) else 0
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && viewModel.filterMode.value == ResepViewModel.FilterMode.HISTORY) {
                    val itemView = viewHolder.itemView
                    val itemHeight = itemView.bottom - itemView.top
                    val isCanceled = dX == 0f && !isCurrentlyActive

                    if (!isCanceled) {
                        // Draw red background
                        val backgroundPaint = Paint().apply { color = Color.parseColor("#EF4444") }
                        
                        if (dX > 0) { // Swiping Right
                            c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat(), backgroundPaint)
                        } else if (dX < 0) { // Swiping Left
                            c.drawRect(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), backgroundPaint)
                        }

                        // Draw delete icon
                        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
                        icon?.let {
                            val iconMargin = (itemHeight - it.intrinsicHeight) / 2
                            val iconTop = itemView.top + (itemHeight - it.intrinsicHeight) / 2
                            val iconBottom = iconTop + it.intrinsicHeight

                            if (dX > 0) { // Swiping Right
                                val iconLeft = itemView.left + iconMargin
                                val iconRight = itemView.left + iconMargin + it.intrinsicWidth
                                it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            } else if (dX < 0) { // Swiping Left
                                val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                                val iconRight = itemView.right - iconMargin
                                it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            }
                            
                            it.setTint(Color.parseColor("#F9FAFB")) // Off-white/Light grey for better professional look
                            it.draw(c)
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvResep)
    }

    private fun showDeleteHistoryDialog(resep: ResepEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Riwayat")
            .setMessage("Hapus \"${resep.judul}\" dari riwayat terakhir dibaca?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.removeFromHistory(resep)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupSearch() {
        binding.etSearchResep.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTabs() {
        binding.tabLayoutResep.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val mode = when (tab?.position) {
                    1 -> ResepViewModel.FilterMode.SAVED
                    2 -> ResepViewModel.FilterMode.HISTORY
                    else -> ResepViewModel.FilterMode.ALL
                }
                viewModel.setFilterMode(mode)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupEmptyState() {
        binding.emptyStateResep.btnRetry.setOnClickListener {
            viewModel.syncData()
            adapter.retry()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                val isListEmpty = loadStates.refresh is LoadState.NotLoading && adapter.itemCount == 0
                val isOffline = loadStates.refresh is LoadState.Error && adapter.itemCount == 0
                
                binding.emptyStateResep.root.visibility = if (isListEmpty || isOffline) View.VISIBLE else View.GONE
                
                if (isOffline) {
                    binding.emptyStateResep.ivEmptyIllustration.setImageResource(R.drawable.ic_no_connection)
                    binding.emptyStateResep.tvEmptyTitle.text = "Mode Offline"
                    binding.emptyStateResep.tvEmptyDesc.text = "Data resep belum tersedia. Aktifkan internet untuk sinkronisasi pertama kali."
                    binding.emptyStateResep.btnRetry.visibility = View.VISIBLE
                } else if (isListEmpty) {
                    binding.emptyStateResep.btnRetry.visibility = View.GONE
                    val query = viewModel.searchQuery.value
                    val mode = viewModel.filterMode.value

                    if (query.isNotEmpty()) {
                        binding.emptyStateResep.ivEmptyIllustration.setImageResource(R.drawable.ic_empty_search)
                        binding.emptyStateResep.tvEmptyTitle.text = "Resep tidak ditemukan"
                        binding.emptyStateResep.tvEmptyDesc.text = "Coba sesuaikan kata kunci filter pencarian"
                    } else if (mode == ResepViewModel.FilterMode.SAVED) {
                        binding.emptyStateResep.ivEmptyIllustration.setImageResource(R.drawable.ic_empty_saved)
                        binding.emptyStateResep.tvEmptyTitle.text = "Resep tersimpan masih kosong"
                        binding.emptyStateResep.tvEmptyDesc.text = "Belum ada resep yang Anda simpan ke koleksi"
                    } else if (mode == ResepViewModel.FilterMode.HISTORY) {
                        binding.emptyStateResep.ivEmptyIllustration.setImageResource(R.drawable.history)
                        binding.emptyStateResep.ivEmptyIllustration.imageTintList = android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.hijau_topakuli)
                        )
                        binding.emptyStateResep.tvEmptyTitle.text = "Belum ada riwayat baca"
                        binding.emptyStateResep.tvEmptyDesc.text = "Resep yang Anda baca akan muncul di sini"
                    } else {
                        binding.emptyStateResep.ivEmptyIllustration.imageTintList = null
                        binding.emptyStateResep.ivEmptyIllustration.setImageResource(R.drawable.ic_no_connection)
                        binding.emptyStateResep.tvEmptyTitle.text = "Data Kosong"
                        binding.emptyStateResep.tvEmptyDesc.text = "Tidak ada data resep saat ini"
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Reset setiap kali fragment kembali aktif (misal dari detail atau navigasi bottom nav)
        val showHistory = arguments?.getBoolean("showHistory") ?: false
        if (!showHistory) {
            viewModel.resetState()
            binding.tabLayoutResep.getTabAt(0)?.select()
            binding.etSearchResep.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
