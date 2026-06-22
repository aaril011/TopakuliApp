package com.project.topakuli.ui.admin.beranda

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.ActivityLog
import com.project.topakuli.databinding.ItemAdminActivityListBinding

class AdminActivityAdapter : ListAdapter<ActivityLog, AdminActivityAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminActivityListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemAdminActivityListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ActivityLog) {
            // Set initial
            val initial = if (item.adminName.isNotEmpty()) {
                item.adminName.split(" ").filter { it.isNotEmpty() }.take(2)
                    .map { it[0].uppercase() }.joinToString("")
            } else "AD"
            binding.tvAdminInitial.text = initial

            // Set Title & Description
            binding.tvActivityTitle.text = "Aktivitas ${item.target}"
            binding.tvActivityDesc.text = "${item.adminName} ${item.type.lowercase()} ${item.target.lowercase()} ${item.targetName}"
            
            // Set Time
            binding.tvActivityTime.text = formatTimeAgo(item.timestamp)

            // Set colors based on type
            when (item.type) {
                "Tambah" -> {
                    binding.cardActivityInitial.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.bg_accent_green))
                    binding.tvAdminInitial.setTextColor(ContextCompat.getColor(itemView.context, R.color.hijau_topakuli))
                }
                "Ubah" -> {
                    binding.cardActivityInitial.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.bg_accent_blue))
                    binding.tvAdminInitial.setTextColor(ContextCompat.getColor(itemView.context, R.color.admin_blue))
                }
                "Hapus" -> {
                    binding.cardActivityInitial.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.bg_accent_orange))
                    binding.tvAdminInitial.setTextColor(ContextCompat.getColor(itemView.context, R.color.admin_red))
                }
                else -> {
                    binding.cardActivityInitial.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.bg_accent_teal))
                    binding.tvAdminInitial.setTextColor(ContextCompat.getColor(itemView.context, R.color.admin_teal))
                }
            }
        }

        private fun formatTimeAgo(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            return when {
                diff < 60_000 -> "Baru saja"
                diff < 3600_000 -> "${diff / 60_000} menit lalu"
                diff < 86400_000 -> "${diff / 3600_000} jam lalu"
                else -> "${diff / 86400_000} hari lalu"
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ActivityLog>() {
            override fun areItemsTheSame(oldItem: ActivityLog, newItem: ActivityLog) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ActivityLog, newItem: ActivityLog) = oldItem == newItem
        }
    }
}
