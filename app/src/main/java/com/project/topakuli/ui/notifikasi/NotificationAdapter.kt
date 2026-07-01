package com.project.topakuli.ui.notifikasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.NotificationEntity
import com.project.topakuli.databinding.ItemNotifikasiBinding
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter : ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotifikasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemNotifikasiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotificationEntity) {
            // Click interaction removed as requested
            binding.tvNotifTitle.text = item.title
            binding.tvNotifDesc.text = item.content
            
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            binding.tvNotifTime.text = sdf.format(item.timestamp)

            // unreadDot logic removed since we no longer track read status in local DB
            binding.unreadDot.visibility = View.GONE

            // Set icon and color based on type context
            when (item.type) {
                "added" -> {
                    binding.cardNotifIcon.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.bg_accent_green))
                    binding.ivNotifIcon.setImageResource(R.drawable.herbal)
                    binding.ivNotifIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.hijau_topakuli))
                }
                "recipe" -> {
                    binding.cardNotifIcon.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.bg_accent_blue))
                    binding.ivNotifIcon.setImageResource(R.drawable.icoon_book)
                    binding.ivNotifIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.admin_blue))
                }
                else -> { // update or general notification
                    binding.cardNotifIcon.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.bg_accent_orange))
                    binding.ivNotifIcon.setImageResource(R.drawable.info)
                    binding.ivNotifIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.admin_orange))
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NotificationEntity>() {
            override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity) = oldItem == newItem
        }
    }
}
