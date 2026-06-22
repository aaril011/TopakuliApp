package com.project.topakuli.ui.resep

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.databinding.ItemResepBinding

class ResepAdapter(
    private val onItemClick: (ResepEntity) -> Unit,
    private val onSaveClick: (ResepEntity) -> Unit,
    private val onItemLongClick: ((ResepEntity) -> Unit)? = null
) :
    PagingDataAdapter<ResepEntity, ResepAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resep = getItem(position)
        if (resep != null) {
            holder.bind(resep)
        }
    }

    inner class ViewHolder(private val binding: ItemResepBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resep: ResepEntity) {
            with(binding) {
                tvJudulResep.text = resep.judul
                tvDeskripsiResep.text = resep.deskripsi
                tvTagKategori.text = resep.kategori
                tvTingkatKesulitan.text = resep.tingkatKesulitan
                
                val bahanCount = resep.bahan.split("\n").filter { it.isNotBlank() }.size
                tvTagBahan.text = itemView.context.getString(R.string.format_bahan, bahanCount)

                val saveIcon = if (resep.isSaved) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
                ivSave.setImageResource(saveIcon)
                ivSave.setBackgroundResource(
                    if (resep.isSaved) R.drawable.bg_circle_green else R.drawable.bg_circle_white
                )
                ivSave.setColorFilter(
                    if (resep.isSaved) itemView.context.getColor(android.R.color.white)
                    else itemView.context.getColor(R.color.hijau_topakuli)
                )

                Glide.with(itemView.context)
                    .load(resep.imageUrl)
                    .into(ivResep)

                root.setOnClickListener { onItemClick(resep) }
                root.setOnLongClickListener {
                    onItemLongClick?.invoke(resep)
                    true
                }
                btnLihatDetail.setOnClickListener { onItemClick(resep) }
                ivSave.setOnClickListener { onSaveClick(resep) }
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
