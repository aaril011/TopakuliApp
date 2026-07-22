package com.project.topakuli.ui.admin.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.databinding.ItemFaqRowBinding

data class FaqItem(val question: String, val answer: String, var isExpanded: Boolean = false)

class FaqAdapter : ListAdapter<FaqItem, FaqAdapter.FaqViewHolder>(FaqDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bind(getItem(position), position == itemCount - 1)
    }

    inner class FaqViewHolder(private val binding: ItemFaqRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FaqItem, isLast: Boolean) {
            binding.tvQuestion.text = item.question
            binding.tvAnswer.text = item.answer
            
            binding.tvAnswer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            binding.ivArrow.rotation = if (item.isExpanded) 90f else 0f
            binding.viewDivider.visibility = if (isLast) View.GONE else View.VISIBLE

            binding.layoutFaqItem.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(adapterPosition)
            }
        }
    }

    class FaqDiffCallback : DiffUtil.ItemCallback<FaqItem>() {
        override fun areItemsTheSame(oldItem: FaqItem, newItem: FaqItem): Boolean = oldItem.question == newItem.question
        override fun areContentsTheSame(oldItem: FaqItem, newItem: FaqItem): Boolean = oldItem == newItem
    }
}