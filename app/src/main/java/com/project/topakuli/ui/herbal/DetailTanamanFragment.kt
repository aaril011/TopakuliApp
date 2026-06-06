package com.project.topakuli.ui.herbal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.data.DataDummy
import com.project.topakuli.databinding.FragmentDetailTanamanBinding

class DetailTanamanFragment : Fragment() {

    private var _binding: FragmentDetailTanamanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailTanamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tanamanId = arguments?.getString("tanamanId")
        val tanaman = DataDummy.generateDummyTanaman().find { it.id == tanamanId }

        tanaman?.let { data ->
            with(binding) {
                tvDetailNama.text = data.nama
                tvDetailNamaDaerah.text = data.namaDaerah
                tvDetailNamaLatin.text = data.namaLatin
                tvDetailDeskripsi.text = data.deskripsi
                tvDetailKhasiat.text = data.khasiat

                Glide.with(this@DetailTanamanFragment)
                    .load(data.imageUrl)
                    .into(ivDetailTanaman)

                ivFavoriteDetail.setImageResource(
                    if (data.isFavorit) R.drawable.ic_heart else R.drawable.ic_heart_border
                )

                // Show related recipe if exists
                val relatedResep = DataDummy.generateDummyResep().find { it.tanamanId == data.id }
                if (relatedResep != null) {
                    cardRelatedResep.visibility = View.VISIBLE
                    tvRelatedJudul.text = relatedResep.judul
                    tvRelatedKategori.text = relatedResep.kategori
                    val bahanCount = relatedResep.bahan.split("\n").filter { it.isNotBlank() }.size
                    tvRelatedBahanCount.text = getString(R.string.format_bahan, bahanCount)
                    
                    Glide.with(this@DetailTanamanFragment)
                        .load(relatedResep.imageUrl)
                        .into(ivRelatedResep)

                    cardRelatedResep.setOnClickListener {
                        val bundle = Bundle().apply {
                            putString("resepId", relatedResep.id)
                        }
                        findNavController().navigate(R.id.action_detailTanamanFragment_to_detailResepFragment, bundle)
                    }
                } else {
                    cardRelatedResep.visibility = View.GONE
                }
                ivFavoriteDetail.setOnClickListener {
                    data.isFavorit = !data.isFavorit
                    ivFavoriteDetail.setImageResource(
                        if (data.isFavorit) R.drawable.ic_heart else R.drawable.ic_heart_border
                    )
                }
            }
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
