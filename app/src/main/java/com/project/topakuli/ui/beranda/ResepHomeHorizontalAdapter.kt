package com.project.topakuli.ui.beranda

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.databinding.ItemResepHorizontalBinding

class ResepHomeHorizontalAdapter(
    private val onItemClick: (ResepEntity) -> Unit
) : ListAdapter<ResepEntity, ResepHomeHorizontalAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResepHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemResepHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resep: ResepEntity) {
            with(binding) {
                tvJudulResep.text = resep.judul
                tvTagKategori.text = resep.kategori
                tvTingkatKesulitan.text = resep.tingkatKesulitan

                val ingredientsCount = resep.bahan.split("\n").filter { it.isNotBlank() }.size
                tvTagBahan.text = "$ingredientsCount Bahan"

                Glide.with(itemView.context)
                    .load(resep.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(ivResep)

                root.setOnClickListener { onItemClick(resep) }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ResepEntity>() {
            override fun areItemsTheSame(oldItem: ResepEntity, newItem: ResepEntity): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: ResepEntity, newItem: ResepEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}