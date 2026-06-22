package com.project.topakuli.ui.admin.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.data.local.entity.ChatMessage
import com.project.topakuli.databinding.ItemAdminChatLeftBinding
import com.project.topakuli.databinding.ItemAdminChatRightBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AdminChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderType == "admin") 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            AdminViewHolder(ItemAdminChatRightBinding.inflate(inflater, parent, false))
        } else {
            UserViewHolder(ItemAdminChatLeftBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is AdminViewHolder) holder.bind(message)
        else if (holder is UserViewHolder) holder.bind(message)
    }

    inner class UserViewHolder(private val binding: ItemAdminChatLeftBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            val initials = item.senderName.split(" ").filter { it.isNotEmpty() }.take(2)
                .map { it[0].uppercase() }.joinToString("")
            binding.tvUserInitial.text = if (initials.isNotEmpty()) initials else "US"
            binding.tvMessage.text = item.message
            binding.tvTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(item.timestamp)
        }
    }

    inner class AdminViewHolder(private val binding: ItemAdminChatRightBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            binding.tvAdminInitial.text = "AD" 
            binding.tvAdminInitial.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.context, com.project.topakuli.R.color.hijau_topakuli))
            binding.tvMessage.text = item.message
            binding.tvTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(item.timestamp)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
        }
    }
}
