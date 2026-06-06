package com.project.topakuli.ui.resep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.data.DataDummy
import com.project.topakuli.databinding.FragmentDetailResepBinding

class DetailResepFragment : Fragment() {

    private var _binding: FragmentDetailResepBinding? = null
    private val binding get() = _binding!!

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
        val resep = DataDummy.generateDummyResep().find { it.id == resepId }

        resep?.let { data ->
            with(binding) {
                tvDetailJudul.text = data.judul
                tvDetailResepDeskripsi.text = data.deskripsi
                tvDetailBahan.text = data.bahan
                tvDetailCara.text = data.caraPembuatan
                tvInfoWaktu.text = data.waktuPersiapan
                tvInfoCara.text = data.caraPakai
                tvInfoFrekuensi.text = data.frekuensi
                tvInfoSimpan.text = data.masaSimpan
                
                tvDetailCatatan.text = data.catatan
                cardWarning.visibility = if (data.catatan.isNullOrEmpty()) View.GONE else View.VISIBLE

                Glide.with(this@DetailResepFragment)
                    .load(data.imageUrl)
                    .into(ivDetailResep)

                ivSaveDetail.setOnClickListener {
                    data.isSaved = !data.isSaved
                    // Optionally update icon here if it changes based on state
                }

                // Setup Tags
                setupManfaatTags(data.manfaat)

                // Related Plant
                val relatedTanaman = DataDummy.generateDummyTanaman().find { it.id == data.tanamanId }
                if (relatedTanaman != null) {
                    cardRelatedTanaman.visibility = View.VISIBLE
                    tvRelatedNamaTanaman.text = relatedTanaman.nama
                    tvRelatedNamaDaerah.text = relatedTanaman.namaDaerah
                    tvRelatedNamaLatin.text = relatedTanaman.namaLatin
                    
                    Glide.with(this@DetailResepFragment)
                        .load(relatedTanaman.imageUrl)
                        .into(ivRelatedTanaman)

                    cardRelatedTanaman.setOnClickListener {
                        val bundle = Bundle().apply {
                            putString("tanamanId", relatedTanaman.id)
                        }
                        findNavController().navigate(R.id.action_detailResepFragment_to_detailTanamanFragment, bundle)
                    }
                } else {
                    cardRelatedTanaman.visibility = View.GONE
                }
            }
        }

        binding.ivBackResep.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupManfaatTags(manfaat: String) {
        binding.flexManfaat.removeAllViews()
        val tags = manfaat.split(",").map { it.trim() }
        tags.forEach { tag ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
