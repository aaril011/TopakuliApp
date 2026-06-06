package com.project.topakuli.ui.herbal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.databinding.ItemTanamanBinding

class TanamanAdapter(
    private val onItemClick: (TanamanObatEntity) -> Unit,
    private val onFavoriteClick: (TanamanObatEntity) -> Unit
) : ListAdapter<TanamanObatEntity, TanamanAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTanamanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tanaman = getItem(position)
        holder.bind(tanaman)
    }

    inner class ViewHolder(private val binding: ItemTanamanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tanaman: TanamanObatEntity) {
            with(binding) {
                tvNamaTanaman.text = tanaman.nama
                tvNamaDaerah.text = tanaman.namaDaerah
                tvNamaLatin.text = tanaman.namaLatin
                
                val favoriteIcon = if (tanaman.isFavorit) R.drawable.ic_heart else R.drawable.ic_heart_border
                ivFavorite.setImageResource(favoriteIcon)
                ivFavorite.setBackgroundResource(
                    if (tanaman.isFavorit) R.drawable.bg_circle_green else R.drawable.bg_circle_white
                )
                ivFavorite.setColorFilter(
                    if (tanaman.isFavorit) itemView.context.getColor(android.R.color.white) 
                    else itemView.context.getColor(R.color.hijau_topakuli)
                )

                Glide.with(itemView.context)
                    .load(tanaman.imageUrl)
                    .into(ivTanaman)

                root.setOnClickListener { onItemClick(tanaman) }
                ivFavorite.setOnClickListener { onFavoriteClick(tanaman) }
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
