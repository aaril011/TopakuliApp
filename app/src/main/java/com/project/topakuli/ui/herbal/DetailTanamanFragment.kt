package com.project.topakuli.ui.herbal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.databinding.FragmentDetailTanamanBinding
import com.project.topakuli.databinding.ItemResepTerkaitSmallBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailTanamanFragment : Fragment() {

    private var _binding: FragmentDetailTanamanBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HerbalViewModel by viewModels {
        HerbalViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

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
        if (tanamanId != null) {
            loadTanamanData(tanamanId)
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadTanamanData(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = (requireActivity().application as TopakuliApplication).repository
            val tanaman = withContext(Dispatchers.IO) { repository.getTanamanById(id) }

            tanaman?.let { data ->
                with(binding) {
                    tvDetailNama.text = data.nama
                    tvDetailNamaDaerah.text = data.namaDaerah
                    tvDetailNamaLatin.text = data.namaLatin
                    tvDetailDeskripsi.text = data.deskripsi
                    tvDetailKhasiat.text = data.khasiat

                    Glide.with(this@DetailTanamanFragment)
                        .load(data.imageUrl)
                        .placeholder(R.drawable.herbal)
                        .into(ivDetailTanaman)

                    ivFavoriteDetail.setImageResource(
                        if (data.isFavorit) R.drawable.ic_heart else R.drawable.ic_heart_border
                    )

                    // Load Related Recipes
                    val allResep = repository.allResep.first()
                    val relatedRecipes = allResep.filter { 
                        it.tanamanIds?.split(",")?.contains(data.id) == true 
                    }
                    
                    if (relatedRecipes.isNotEmpty()) {
                        cardRelatedResepContainer.visibility = View.VISIBLE
                        rvRelatedResep.layoutManager = LinearLayoutManager(requireContext())
                        rvRelatedResep.adapter = RelatedResepAdapter(relatedRecipes) { resep ->
                            val bundle = Bundle().apply {
                                putString("resepId", resep.id)
                            }
                            findNavController().navigate(R.id.action_detailTanamanFragment_to_detailResepFragment, bundle)
                        }
                    } else {
                        cardRelatedResepContainer.visibility = View.GONE
                    }
                    
                    ivFavoriteDetail.setOnClickListener {
                        viewModel.toggleFavorit(data)
                        data.isFavorit = !data.isFavorit
                        ivFavoriteDetail.setImageResource(
                            if (data.isFavorit) R.drawable.ic_heart else R.drawable.ic_heart_border
                        )
                    }
                }
            }
        }
    }

    inner class RelatedResepAdapter(
        private val items: List<ResepEntity>,
        private val onClick: (ResepEntity) -> Unit
    ) : RecyclerView.Adapter<RelatedResepAdapter.ViewHolder>() {
        
        inner class ViewHolder(private val binding: ItemResepTerkaitSmallBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: ResepEntity) {
                binding.tvJudul.text = item.judul
                binding.tvKategori.text = item.kategori
                Glide.with(itemView.context).load(item.imageUrl).placeholder(R.drawable.book).into(binding.ivResep)
                binding.root.setOnClickListener { onClick(item) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemResepTerkaitSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
