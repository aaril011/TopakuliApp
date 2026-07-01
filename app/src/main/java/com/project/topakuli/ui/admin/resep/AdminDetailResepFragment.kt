package com.project.topakuli.ui.admin.resep

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
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.databinding.FragmentAdminDetailResepBinding
import com.project.topakuli.databinding.ItemTanamanTerkaitSmallBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDetailResepFragment : Fragment() {

    private var _binding: FragmentAdminDetailResepBinding? = null
    private val binding get() = _binding!!
    private var resepId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDetailResepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resepId = arguments?.getString("EXTRA_RESEP_ID")
        if (resepId != null) {
            loadResepData(resepId!!)
        }

        setupActions()
    }

    private fun loadResepData(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = (requireActivity().application as TopakuliApplication).repository
            val resep = withContext(Dispatchers.IO) { repository.getResepById(id) }
            if (resep != null) {
                binding.apply {
                    tvJudul.text = resep.judul
                    tvKategori.text = "Kategori: ${resep.kategori}"
                    tvKesulitan.text = "Kesulitan: ${resep.tingkatKesulitan}"
                    tvDeskripsi.text = resep.deskripsi
                    tvManfaat.text = resep.manfaat
                    tvBahan.text = resep.bahan
                    tvCara.text = resep.caraPembuatan
                    tvInfoWaktu.text = resep.waktuPersiapan.ifEmpty { "-" }
                    tvInfoCara.text = resep.caraPakai.ifEmpty { "-" }
                    tvInfoFrekuensi.text = resep.frekuensi.ifEmpty { "-" }
                    tvInfoSimpan.text = resep.masaSimpan.ifEmpty { "-" }
                    
                    if (!resep.catatan.isNullOrEmpty()) {
                        cardCatatan.visibility = View.VISIBLE
                        tvCatatan.text = resep.catatan
                    } else {
                        cardCatatan.visibility = View.GONE
                    }
                    
                    // Load Tanaman Terkait List
                    val ids = resep.tanamanIds?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                    if (ids.isNotEmpty()) {
                        val relatedList = mutableListOf<TanamanObatEntity>()
                        ids.forEach { tanamanId ->
                            val tanaman = withContext(Dispatchers.IO) { repository.getTanamanById(tanamanId) }
                            tanaman?.let { relatedList.add(it) }
                        }
                        
                        if (relatedList.isNotEmpty()) {
                            cardTanamanTerkaitContainer.visibility = View.VISIBLE
                            rvTanamanTerkait.layoutManager = LinearLayoutManager(requireContext())
                            rvTanamanTerkait.adapter = RelatedTanamanAdapter(relatedList) { tanaman ->
                                val bundle = Bundle().apply {
                                    putString("EXTRA_TANAMAN_ID", tanaman.id)
                                }
                                findNavController().navigate(R.id.admin_detail_tanaman, bundle)
                            }
                        } else {
                            cardTanamanTerkaitContainer.visibility = View.GONE
                        }
                    } else {
                        cardTanamanTerkaitContainer.visibility = View.GONE
                    }

                    Glide.with(this@AdminDetailResepFragment)
                        .load(resep.imageUrl)
                        .placeholder(R.drawable.icoon_book)
                        .error(R.drawable.icoon_book)
                        .into(ivResepMain)

                    ivResepMain.setOnClickListener {
                        resep.imageUrl?.let { showImagePreview(it) }
                    }
                }
            }
        }
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

    inner class RelatedTanamanAdapter(
        private val items: List<TanamanObatEntity>,
        private val onClick: (TanamanObatEntity) -> Unit
    ) : RecyclerView.Adapter<RelatedTanamanAdapter.ViewHolder>() {
        
        inner class ViewHolder(private val binding: ItemTanamanTerkaitSmallBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: TanamanObatEntity) {
                binding.tvNama.text = item.nama
                binding.tvLatin.text = item.namaLatin
                Glide.with(itemView.context).load(item.imageUrl).placeholder(R.drawable.herbal).into(binding.ivTanaman)
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

    private fun setupActions() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.fabEditResep.setOnClickListener {
            val bundle = Bundle().apply {
                putString("EXTRA_RESEP_ID", resepId)
            }
            findNavController().navigate(R.id.admin_add_resep, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
