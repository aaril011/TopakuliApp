package com.project.topakuli.ui.admin.herbal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.databinding.ItemTanamanAdminBinding

class ManageTanamanAdapter(
    private val onEditClick: (TanamanObatEntity) -> Unit,
    private val onItemClick: (TanamanObatEntity) -> Unit
) : PagingDataAdapter<TanamanObatEntity, ManageTanamanAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTanamanAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(private val binding: ItemTanamanAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tanaman: TanamanObatEntity) {
            binding.apply {
                tvNama.text = tanaman.nama
                tvNamaDaerah.text = tanaman.namaDaerah
                tvNamaLatin.text = tanaman.namaLatin

                Glide.with(root.context)
                    .load(tanaman.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .thumbnail(0.1f)
                    .into(ivTanaman)

                btnEdit.setOnClickListener { onEditClick(tanaman) }
                root.setOnClickListener { onItemClick(tanaman) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TanamanObatEntity>() {
            override fun areItemsTheSame(oldItem: TanamanObatEntity, newItem: TanamanObatEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TanamanObatEntity, newItem: TanamanObatEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
