package com.project.topakuli.ui.history

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.HistoryEntity
import com.project.topakuli.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onSelectionChanged: (Int) -> Unit,
    private val onItemClick: (HistoryEntity) -> Unit
) : ListAdapter<HistoryEntity, HistoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var isSelectionMode = false
    private val selectedIds = mutableSetOf<Int>()

    fun setSelectionMode(enabled: Boolean) {
        if (isSelectionMode != enabled) {
            isSelectionMode = enabled
            if (!enabled) selectedIds.clear()
            notifyDataSetChanged()
            onSelectionChanged(selectedIds.size)
        }
    }

    fun isSelectionMode() = isSelectionMode

    fun getSelectedIds(): List<Int> = selectedIds.toList()

    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = getItem(position)
        holder.bind(history)
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(history: HistoryEntity) {
            with(binding) {
                val displayName = if (history.namaLatin.isNotEmpty()) {
                    "${history.namaTanaman} (${history.namaLatin})"
                } else {
                    history.namaTanaman
                }
                tvHistoryName.text = displayName
                tvHistoryAccuracy.text = String.format("%.1f%%", history.akurasi)
                tvHistoryDate.text = formatTimestamp(history.timestamp)

                // Robust Image Loading
                Glide.with(itemView)
                    .load(history.imagePath)
                    .apply(RequestOptions()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(ivHistoryImage)

                // Selection UI
                cbSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
                cbSelect.isChecked = selectedIds.contains(history.id)
                ivArrow.visibility = if (isSelectionMode) View.GONE else View.VISIBLE

                root.setOnClickListener {
                    if (isSelectionMode) {
                        toggleSelection(history.id)
                    } else {
                        onItemClick(history)
                    }
                }

                cbSelect.setOnClickListener {
                    toggleSelection(history.id)
                }
            }
        }

        private fun toggleSelection(id: Int) {
            if (selectedIds.contains(id)) {
                selectedIds.remove(id)
            } else {
                selectedIds.add(id)
            }
            binding.cbSelect.isChecked = selectedIds.contains(id)
            onSelectionChanged(selectedIds.size)
        }

        private fun formatTimestamp(timestamp: Long): String {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID"))
            val timeString = timeFormat.format(Date(timestamp))

            return when {
                DateUtils.isToday(timestamp) -> {
                    "Hari ini pukul $timeString"
                }
                DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS) -> {
                    "Kemarin pukul $timeString"
                }
                else -> {
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HistoryEntity>() {
            override fun areItemsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
