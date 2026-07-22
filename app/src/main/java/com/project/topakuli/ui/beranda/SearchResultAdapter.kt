package com.project.topakuli.ui.beranda

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.databinding.ItemResepHomeBinding

class SearchResultAdapter(
    private val onItemClick: (id: String, type: String) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    private var items = mutableListOf<SearchItem>()

    fun submitList(newItems: List<SearchItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResepHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemResepHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem) {
            binding.tvJudulResep.text = item.title
            binding.tvTagKategori.text = item.subtitle
            binding.tvTagBahan.text = item.type
            
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(binding.ivResep)
                
            binding.root.setOnClickListener { onItemClick(item.id, item.type) }
        }
    }

    data class SearchItem(
        val id: String,
        val title: String,
        val subtitle: String,
        val imageUrl: String?,
        val type: String // "Tanaman" atau "Resep"
    )
}