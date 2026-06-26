package com.project.topakuli.ui.admin.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.data.remote.model.ContributionForm
import com.project.topakuli.databinding.ItemAdminMasukanBinding
import java.text.SimpleDateFormat
import java.util.*

class AdminContributionAdapter(private val onClick: (ContributionForm) -> Unit) :
    ListAdapter<ContributionForm, AdminContributionAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminMasukanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAdminMasukanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContributionForm) {
            with(binding) {
                tvUserName.text = item.userName
                
                when(item.type) {
                    "resep" -> {
                        tvTitleSubmission.text = item.recipeName ?: "Ajuan Resep Tanpa Judul"
                        tvSubtitleSubmission.text = "Keluhan: ${item.recipeSymptoms ?: "-"}"
                    }
                    "tanaman" -> {
                        tvTitleSubmission.text = item.plantName ?: "Ajuan Tanaman Tanpa Nama"
                        tvSubtitleSubmission.text = "Nama Daerah: ${item.plantLocalName ?: "-"}"
                    }
                    "koreksi" -> {
                        tvTitleSubmission.text = "Koreksi: ${item.targetItemName ?: "Data"}"
                        tvSubtitleSubmission.text = item.correctionPart ?: "-"
                    }
                    else -> {
                        tvTitleSubmission.text = "Ajuan Baru"
                        tvSubtitleSubmission.text = "-"
                    }
                }
                
                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                tvTime.text = sdf.format(Date(item.timestamp))
                
                val initials = item.userName.split(" ").filter { it.isNotEmpty() }.take(2)
                    .map { it[0].uppercase() }.joinToString("")
                tvUserInitial.text = if (initials.isNotEmpty()) initials else "US"
                
                // Indikator belum dibaca (titik hijau)
                unreadIndicator.visibility = if (!item.isReadByAdmin) View.VISIBLE else View.GONE
                
                root.setOnClickListener { onClick(item) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ContributionForm>() {
            override fun areItemsTheSame(oldItem: ContributionForm, newItem: ContributionForm): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ContributionForm, newItem: ContributionForm): Boolean =
                oldItem == newItem
        }
    }
}
