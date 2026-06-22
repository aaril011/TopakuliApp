package com.project.topakuli.ui.admin.beranda

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.databinding.FragmentAdminActivityListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminActivityListFragment : Fragment() {

    private var _binding: FragmentAdminActivityListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminDashboardViewModel by viewModels {
        AdminDashboardViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    private lateinit var adapter: AdminActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminActivityListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        setupSwipeToDelete()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        adapter = AdminActivityAdapter()
        binding.rvActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@AdminActivityListFragment.adapter
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = adapter.currentList[position]
                viewModel.deleteActivityLog(item.id)
                Toast.makeText(requireContext(), "Aktivitas berhasil dihapus", Toast.LENGTH_SHORT).show()
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
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
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
                            
                            it.setTint(Color.parseColor("#F9FAFB"))
                            it.draw(c)
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvActivities)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentActivities.collectLatest { list ->
                    adapter.submitList(list)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
