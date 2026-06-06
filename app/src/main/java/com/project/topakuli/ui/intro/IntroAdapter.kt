package com.project.topakuli.ui.intro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.databinding.ItemIntroSlideBinding

class IntroAdapter(private val slides: List<IntroSlide>) :
    RecyclerView.Adapter<IntroAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIntroSlideBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(slides[position])
    }

    override fun getItemCount(): Int = slides.size

    inner class ViewHolder(private val binding: ItemIntroSlideBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(slide: IntroSlide) {
            binding.tvTitle.text = slide.title
            binding.tvDescription.text = slide.description
            binding.ivIntroImage.setImageResource(slide.image)
        }
    }
}