package com.project.topakuli.ui.herbal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.databinding.ItemTanamanBinding
import com.project.topakuli.databinding.ItemTanamanListBinding

class TanamanAdapter(
    private val onItemClick: (TanamanObatEntity) -> Unit,
    private val onFavoriteClick: (TanamanObatEntity) -> Unit
) : PagingDataAdapter<TanamanObatEntity, TanamanAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var isGridView: Boolean = true

    fun setViewType(isGrid: Boolean) {
        this.isGridView = isGrid
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridView) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = if (viewType == VIEW_TYPE_GRID) {
            ItemTanamanBinding.inflate(inflater, parent, false)
        } else {
            ItemTanamanListBinding.inflate(inflater, parent, false)
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tanaman = getItem(position)
        if (tanaman != null) {
            holder.bind(tanaman)
        }
    }

    inner class ViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tanaman: TanamanObatEntity) {
            when (binding) {
                is ItemTanamanBinding -> {
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
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(ivTanaman)

                        root.setOnClickListener { onItemClick(tanaman) }
                        ivFavorite.setOnClickListener { onFavoriteClick(tanaman) }
                    }
                }
                is ItemTanamanListBinding -> {
                    with(binding) {
                        tvNamaTanaman.text = tanaman.nama
                        tvNamaDaerah.text = tanaman.namaDaerah
                        tvNamaLatin.text = tanaman.namaLatin

                        val favoriteIcon = if (tanaman.isFavorit) R.drawable.ic_heart else R.drawable.ic_heart_border
                        ivFavorite.setImageResource(favoriteIcon)
                        ivFavorite.setBackgroundResource(
                            if (tanaman.isFavorit) R.drawable.bg_circle_white else R.drawable.bg_circle_white
                        )
                        ivFavorite.setColorFilter(
                            if (tanaman.isFavorit) itemView.context.getColor(R.color.hijau_topakuli)
                            else itemView.context.getColor(R.color.hijau_topakuli)
                        )
                        // For list view, we use ic_heart and ic_heart_border but maybe with different coloring logic
                        // Actually let's keep it consistent with the other one if needed, but the list design I made has white bg.
                        
                        if (tanaman.isFavorit) {
                            ivFavorite.setImageResource(R.drawable.ic_heart)
                        } else {
                            ivFavorite.setImageResource(R.drawable.ic_heart_border)
                        }

                        Glide.with(itemView.context)
                            .load(tanaman.imageUrl)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(ivTanaman)

                        root.setOnClickListener { onItemClick(tanaman) }
                        ivFavorite.setOnClickListener { onFavoriteClick(tanaman) }
                    }
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_GRID = 1
        private const val VIEW_TYPE_LIST = 2

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
