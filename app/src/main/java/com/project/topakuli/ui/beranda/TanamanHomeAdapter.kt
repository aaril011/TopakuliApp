package com.project.topakuli.ui.beranda

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.databinding.ItemTanamanHomeBinding

class TanamanHomeAdapter(
    private val onItemClick: (TanamanObatEntity) -> Unit
) : ListAdapter<TanamanObatEntity, TanamanHomeAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTanamanHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tanaman = getItem(position)
        holder.bind(tanaman)
    }

    inner class ViewHolder(private val binding: ItemTanamanHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tanaman: TanamanObatEntity) {
            with(binding) {
                tvNamaTanaman.text = tanaman.nama
                tvNamaDaerah.text = tanaman.namaDaerah
                tvNamaLatin.text = tanaman.namaLatin

                Glide.with(itemView.context)
                    .load(tanaman.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(ivTanaman)

                root.setOnClickListener { onItemClick(tanaman) }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TanamanObatEntity>() {
            override fun areItemsTheSame(oldItem: TanamanObatEntity, newItem: TanamanObatEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TanamanObatEntity, newItem: TanamanObatEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}