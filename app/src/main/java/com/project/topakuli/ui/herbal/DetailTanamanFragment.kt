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
import com.project.topakuli.data.local.entity.TanamanImageEntity
import com.project.topakuli.databinding.FragmentDetailTanamanBinding
import com.project.topakuli.databinding.ItemResepTerkaitSmallBinding
import com.project.topakuli.databinding.ItemTanamanPhotoThumbnailBinding
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
            if (arguments?.getBoolean("from_notification") == true && arguments?.getBoolean("is_from_list") == true) {
                activity?.finish()
            } else {
                findNavController().navigateUp()
            }
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
                    
                    ivDetailTanaman.setOnClickListener {
                        data.imageUrl?.let { showImagePreview(it) }
                    }

                    // Sync & Load Additional Photos
                    viewLifecycleOwner.lifecycleScope.launch {
                        repository.syncTanamanImages(id)
                        repository.getTanamanImages(id).collect { images ->
                            if (images.isNotEmpty()) {
                                cardFotoLainnya.visibility = View.VISIBLE
                                rvFotoLainnya.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                                rvFotoLainnya.adapter = TanamanGalleryAdapter(images)
                            } else {
                                cardFotoLainnya.visibility = View.GONE
                            }
                        }
                    }

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

                    ivShare.setOnClickListener {
                        val shareText = "Informasi Tanaman Obat: ${data.nama}\n" +
                                "Nama Latin: ${data.namaLatin}\n\n" +
                                "Khasiat: ${data.khasiat}\n\n" +
                                "Dapatkan informasi lebih lengkap di aplikasi Topakuli!"
                        
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Detail Tanaman ${data.nama}")
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        startActivity(android.content.Intent.createChooser(intent, "Bagikan via"))
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

    inner class TanamanGalleryAdapter(
        private val items: List<TanamanImageEntity>
    ) : RecyclerView.Adapter<TanamanGalleryAdapter.ViewHolder>() {

        inner class ViewHolder(private val binding: ItemTanamanPhotoThumbnailBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: TanamanImageEntity) {
                Glide.with(itemView.context).load(item.imageUrl).into(binding.ivThumbnail)
                binding.btnRemove.visibility = View.GONE // Hide remove button in detail view
                
                binding.root.setOnClickListener {
                    showImagePreview(item.imageUrl)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTanamanPhotoThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size
    }

    private fun showImagePreview(imageUrl: String) {
        val dialog = android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_preview)
        
        val imageView = dialog.findViewById<android.widget.ImageView>(R.id.iv_full_preview)
        val btnClose = dialog.findViewById<View>(R.id.btn_close)
        
        Glide.with(this).load(imageUrl).into(imageView)
        
        // Tap gambar atau tombol close untuk menutup
        imageView.setOnClickListener { dialog.dismiss() }
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
