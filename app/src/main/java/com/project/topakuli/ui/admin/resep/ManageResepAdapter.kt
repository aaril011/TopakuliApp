package com.project.topakuli.ui.admin.resep

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.databinding.ItemResepAdminBinding

class ManageResepAdapter(
    private val onEditClick: (ResepEntity) -> Unit,
    private val onItemClick: (ResepEntity) -> Unit
) : PagingDataAdapter<ResepEntity, ManageResepAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResepAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(private val binding: ItemResepAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(resep: ResepEntity) {
            binding.apply {
                tvJudul.text = resep.judul
                tvDeskripsi.text = resep.deskripsi
                tvKategori.text = resep.kategori
                tvTagKesulitan.text = resep.tingkatKesulitan

                val bahanCount = resep.bahan.split("\n").filter { it.isNotBlank() }.size
                tvTagBahan.text = itemView.context.getString(R.string.format_bahan, bahanCount)

                Glide.with(root.context)
                    .load(resep.imageUrl)
                    .placeholder(R.drawable.intro_1)
                    .thumbnail(0.1f)
                    .into(ivResep)

                btnEdit.setOnClickListener { onEditClick(resep) }
                root.setOnClickListener { onItemClick(resep) }
                btnLihatDetail.setOnClickListener { onItemClick(resep) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ResepEntity>() {
            override fun areItemsTheSame(oldItem: ResepEntity, newItem: ResepEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ResepEntity, newItem: ResepEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
