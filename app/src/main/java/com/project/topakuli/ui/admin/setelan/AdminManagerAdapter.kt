package com.project.topakuli.ui.admin.setelan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.databinding.ItemAdminManagerBinding

data class ManagerModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = ""
)

class AdminManagerAdapter(private val onItemClick: (ManagerModel) -> Unit) : ListAdapter<ManagerModel, AdminManagerAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemAdminManagerBinding,
        private val onItemClick: (ManagerModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ManagerModel) {
            binding.tvManagerName.text = item.name
            binding.tvManagerEmail.text = item.email
            binding.tvManagerPhone.text = item.phoneNumber.ifEmpty { "-" }
            
            val initials = item.name.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it[0].uppercase() }
                .joinToString("")
            binding.tvManagerInitial.text = if (initials.isNotEmpty()) initials else "AD"

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ManagerModel>() {
            override fun areItemsTheSame(oldItem: ManagerModel, newItem: ManagerModel) = oldItem.uid == newItem.uid
            override fun areContentsTheSame(oldItem: ManagerModel, newItem: ManagerModel) = oldItem == newItem
        }
    }
}
