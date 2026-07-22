package com.project.topakuli.ui.resep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.databinding.FragmentDetailResepBinding
import com.project.topakuli.databinding.ItemTanamanTerkaitSmallBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailResepFragment : Fragment() {

    private var _binding: FragmentDetailResepBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResepViewModel by viewModels {
        ResepViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailResepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resepId = arguments?.getString("resepId")
        if (resepId != null) {
            loadResepData(resepId)
            markAsRead(resepId)
        }

        binding.ivBackResep.setOnClickListener {
            if (arguments?.getBoolean("from_notification") == true && arguments?.getBoolean("is_from_list") == true) {
                activity?.finish()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun markAsRead(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = (requireActivity().application as TopakuliApplication).repository
            withContext(Dispatchers.IO) { repository.markResepAsRead(id) }
        }
    }

    private fun loadResepData(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = (requireActivity().application as TopakuliApplication).repository
            val resep = withContext(Dispatchers.IO) { repository.getResepById(id) }
            
            resep?.let { data ->
                with(binding) {
                    tvDetailJudul.text = data.judul
                    tvDetailResepDeskripsi.text = formatListText(data.deskripsi)
                    tvDetailBahan.text = formatListText(data.bahan)
                    tvDetailCara.text = formatListText(data.caraPembuatan)
                    tvInfoWaktu.text = data.waktuPersiapan.ifEmpty { "-" }
                    tvInfoCara.text = data.caraPakai.ifEmpty { "-" }
                    tvInfoFrekuensi.text = data.frekuensi.ifEmpty { "-" }
                    tvInfoSimpan.text = data.masaSimpan.ifEmpty { "-" }
                    
                    tvDetailCatatan.text = formatListText(data.catatan ?: "")
                    cardWarning.visibility = if (data.catatan.isNullOrEmpty()) View.GONE else View.VISIBLE

                    Glide.with(this@DetailResepFragment)
                        .load(data.imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(ivDetailResep)

                    ivDetailResep.setOnClickListener {
                        data.imageUrl?.let { showImagePreview(it) }
                    }

                    updateFavoriteIcon(data.isSaved)
                    ivSaveDetail.setOnClickListener {
                        viewModel.toggleSave(data)
                        data.isSaved = !data.isSaved
                        updateFavoriteIcon(data.isSaved)
                    }

                    ivShareResep.setOnClickListener {
                        val shareText = "Resep Obat Herbal: ${data.judul}\n\n" +
                                "Bahan-bahan:\n${data.bahan}\n\n" +
                                "Cara Pembuatan:\n${data.caraPembuatan}\n\n" +
                                "Dapatkan resep herbal lainnya di aplikasi Topakuli!"
                        
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Resep Herbal: ${data.judul}")
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        startActivity(android.content.Intent.createChooser(intent, "Bagikan via"))
                    }

                    // Setup Tags
                    setupManfaatTags(data.manfaat)

                    // Related Plants List
                    val ids = data.tanamanIds?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                    if (ids.isNotEmpty()) {
                        val relatedList = mutableListOf<TanamanObatEntity>()
                        ids.forEach { tanamanId ->
                            val tanaman = withContext(Dispatchers.IO) { repository.getTanamanById(tanamanId) }
                            tanaman?.let { relatedList.add(it) }
                        }
                        
                        if (relatedList.isNotEmpty()) {
                            rvRelatedTanaman.visibility = View.VISIBLE
                            rvRelatedTanaman.layoutManager = LinearLayoutManager(requireContext())
                            rvRelatedTanaman.adapter = RelatedTanamanAdapter(relatedList) { tanaman ->
                                val bundle = Bundle().apply {
                                    putString("tanamanId", tanaman.id)
                                }
                                findNavController().navigate(R.id.action_detailResepFragment_to_detailTanamanFragment, bundle)
                            }
                        } else {
                            rvRelatedTanaman.visibility = View.GONE
                        }
                    } else {
                        rvRelatedTanaman.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun updateFavoriteIcon(isSaved: Boolean) {
        binding.ivSaveDetail.setImageResource(
            if (isSaved) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
        )
    }

    private fun showImagePreview(imageUrl: String) {
        val dialog = android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_preview)
        
        val imageView = dialog.findViewById<android.widget.ImageView>(R.id.iv_full_preview)
        val btnClose = dialog.findViewById<View>(R.id.btn_close)
        
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(imageView)
        
        // Tap gambar atau tombol close untuk menutup
        imageView.setOnClickListener { dialog.dismiss() }
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun setupManfaatTags(manfaat: String) {
        binding.flexManfaat.removeAllViews()
        val tags = manfaat.split(",").map { it.trim() }
        tags.forEach { tag ->
            if (tag.isNotEmpty()) {
                val textView = TextView(requireContext()).apply {
                    text = tag
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.hijau_topakuli))
                    setBackgroundResource(R.drawable.bg_tag_manfaat)
                    setPadding(24, 8, 24, 8)
                    
                    val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_circle)
                    drawable?.setBounds(0, 0, 32, 32)
                    drawable?.setTint(ContextCompat.getColor(requireContext(), R.color.hijau_topakuli))
                    setCompoundDrawables(drawable, null, null, null)
                    compoundDrawablePadding = 8

                    val params = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 16, 16)
                    layoutParams = params
                }
                binding.flexManfaat.addView(textView)
            }
        }
    }

    private fun formatListText(text: String): CharSequence {
        val lines = text.split("\n")
        val builder = android.text.SpannableStringBuilder()
        
        lines.forEachIndexed { index, line ->
            if (line.isBlank()) return@forEachIndexed
            
            val start = builder.length
            builder.append(line)
            val end = builder.length
            
            // Check if line starts with a list marker (e.g., "1. ", "• ", "- ", "* ")
            val isNumbered = Regex("^\\d+\\.\\s").containsMatchIn(line)
            val isBulleted = Regex("^[•\\-\\*]\\s").containsMatchIn(line)
            
            if (isNumbered || isBulleted) {
                // Apply hanging indent (LeadingMarginSpan)
                // first: margin for the first line
                // rest: margin for the subsequent lines
                // We want the rest to be indented more than the first
                val indent = 48 // adjust as needed
                builder.setSpan(
                    android.text.style.LeadingMarginSpan.Standard(0, indent),
                    start,
                    end,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            
            if (index < lines.size - 1) {
                builder.append("\n")
            }
        }
        return builder
    }

    inner class RelatedTanamanAdapter(
        private val items: List<TanamanObatEntity>,
        private val onClick: (TanamanObatEntity) -> Unit
    ) : RecyclerView.Adapter<RelatedTanamanAdapter.ViewHolder>() {
        
        inner class ViewHolder(private val binding: ItemTanamanTerkaitSmallBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: TanamanObatEntity) {
                binding.tvNama.text = item.nama
                binding.tvLatin.text = item.namaLatin
                Glide.with(itemView.context).load(item.imageUrl).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(binding.ivTanaman)
                binding.root.setOnClickListener { onClick(item) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTanamanTerkaitSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
