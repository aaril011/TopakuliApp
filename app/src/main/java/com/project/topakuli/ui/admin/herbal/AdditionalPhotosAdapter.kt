package com.project.topakuli.ui.admin.herbal

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.databinding.ItemTanamanPhotoThumbnailBinding

class AdditionalPhotosAdapter(
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<AdditionalPhotosAdapter.ViewHolder>() {

    private val photos = mutableListOf<Uri>()

    fun setPhotos(newPhotos: List<Uri>) {
        photos.clear()
        photos.addAll(newPhotos)
        notifyDataSetChanged()
    }

    fun addPhoto(uri: Uri) {
        photos.add(uri)
        notifyItemInserted(photos.size - 1)
    }

    fun removePhoto(position: Int) {
        photos.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, photos.size)
    }

    fun getPhotos(): List<Uri> = photos

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTanamanPhotoThumbnailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photos[position], position)
    }

    override fun getItemCount(): Int = photos.size

    inner class ViewHolder(private val binding: ItemTanamanPhotoThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(uri: Uri, position: Int) {
            Glide.with(binding.root.context)
                .load(uri)
                .into(binding.ivThumbnail)

            binding.btnRemove.setOnClickListener {
                onRemoveClick(position)
            }
        }
    }
}
