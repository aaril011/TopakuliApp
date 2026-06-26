package com.project.topakuli.ui.kontribusi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.R
import com.project.topakuli.databinding.ItemResepTerkaitSmallBinding

class KontribusiAdapter {

    data class SearchItem(val id: String, val name: String, val sub: String, val type: String)

    class SearchResultAdapter(private val onClick: (SearchItem) -> Unit) : ListAdapter<SearchItem, SearchResultAdapter.VH>(
        object : DiffUtil.ItemCallback<SearchItem>() {
            override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem) = oldItem == newItem
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(ItemResepTerkaitSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = getItem(position)
            holder.binding.tvJudul.text = item.name
            holder.binding.tvKategori.text = item.sub
            holder.binding.ivResep.setImageResource(if (item.type == "tanaman") R.drawable.herbal else R.drawable.icoon_book)
            holder.itemView.setOnClickListener { onClick(item) }
        }

        inner class VH(val binding: ItemResepTerkaitSmallBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
