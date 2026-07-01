package com.project.topakuli.ui.admin.herbal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanImageEntity
import com.project.topakuli.databinding.FragmentAdminDetailTanamanBinding
import com.project.topakuli.databinding.ItemResepTerkaitSmallBinding
import com.project.topakuli.databinding.ItemTanamanPhotoThumbnailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDetailTanamanFragment : Fragment() {

    private var _binding: FragmentAdminDetailTanamanBinding? = null
    private val binding get() = _binding!!
    private var tanamanId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDetailTanamanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tanamanId = arguments?.getString("EXTRA_TANAMAN_ID")
        if (tanamanId != null) {
            loadTanamanData(tanamanId!!)
        }

        setupActions()
    }

    private fun loadTanamanData(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = (requireActivity().application as TopakuliApplication).repository
            val tanaman = withContext(Dispatchers.IO) { repository.getTanamanById(id) }
            if (tanaman != null) {
                binding.apply {
                    tvNama.text = tanaman.nama
                    tvNamaDaerah.text = tanaman.namaDaerah
                    tvNamaLatin.text = tanaman.namaLatin
                    tvDeskripsi.text = tanaman.deskripsi
                    tvKhasiat.text = tanaman.khasiat

                    Glide.with(this@AdminDetailTanamanFragment)
                        .load(tanaman.imageUrl)
                        .placeholder(R.drawable.herbal)
                        .error(R.drawable.herbal)
                        .into(ivTanamanMain)
                    
                    ivTanamanMain.setOnClickListener {
                        tanaman.imageUrl?.let { showImagePreview(it) }
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
                        
                    // Load Related Recipes
                    val allResep = repository.allResep.first()
                    val relatedRecipes = allResep.filter { 
                        it.tanamanIds?.split(",")?.contains(tanaman.id) == true 
                    }
                    
                    if (relatedRecipes.isNotEmpty()) {
                        cardResepTerkaitContainer.visibility = View.VISIBLE
                        rvResepTerkait.layoutManager = LinearLayoutManager(requireContext())
                        rvResepTerkait.adapter = RelatedResepAdapter(relatedRecipes) { resep ->
                            val bundle = Bundle().apply {
                                putString("EXTRA_RESEP_ID", resep.id)
                            }
                            findNavController().navigate(R.id.admin_detail_resep, bundle)
                        }
                    } else {
                        cardResepTerkaitContainer.visibility = View.GONE
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
        dialog.setContentView(R.layout.dialog_image_preview) // We will create this layout
        
        val imageView = dialog.findViewById<android.widget.ImageView>(R.id.iv_full_preview)
        val btnClose = dialog.findViewById<View>(R.id.btn_close)
        
        Glide.with(this).load(imageUrl).into(imageView)
        
        // Tap gambar atau tombol close untuk menutup
        imageView.setOnClickListener { dialog.dismiss() }
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun setupActions() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.fabEditTanaman.setOnClickListener {
            val bundle = Bundle().apply {
                putString("EXTRA_TANAMAN_ID", tanamanId)
            }
            findNavController().navigate(R.id.admin_add_tanaman, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
