package com.project.topakuli.ui.admin.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.data.local.entity.UserContribution
import com.project.topakuli.databinding.ItemAdminMasukanBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AdminMasukanAdapter(private val onClick: (UserContribution) -> Unit) :
    ListAdapter<UserContribution, AdminMasukanAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminMasukanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAdminMasukanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserContribution) {
            val initials = item.userName.split(" ").filter { it.isNotEmpty() }.take(2)
                .map { it[0].uppercase() }.joinToString("")
            binding.tvUserInitial.text = if (initials.isNotEmpty()) initials else "US"
            
            binding.tvUserName.text = item.userName
            binding.tvLastMessage.text = item.lastMessage
            
            // Show email or phone in the list if available
            val contactInfo = when {
                item.userEmail.isNotEmpty() && item.userPhone.isNotEmpty() -> "${item.userEmail} • ${item.userPhone}"
                item.userEmail.isNotEmpty() -> item.userEmail
                item.userPhone.isNotEmpty() -> item.userPhone
                else -> ""
            }
            
            if (contactInfo.isNotEmpty()) {
                binding.tvLastMessage.text = "$contactInfo\n${item.lastMessage}"
            }

            binding.tvTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(item.timestamp)
            binding.unreadIndicator.visibility = if (item.isReadByAdmin) View.GONE else View.VISIBLE
            
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserContribution>() {
            override fun areItemsTheSame(oldItem: UserContribution, newItem: UserContribution) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: UserContribution, newItem: UserContribution) = oldItem == newItem
        }
    }
}
