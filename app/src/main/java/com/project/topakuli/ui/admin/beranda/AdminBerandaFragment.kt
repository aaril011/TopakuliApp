package com.project.topakuli.ui.admin.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.databinding.FragmentAdminBerandaBinding
import kotlinx.coroutines.launch

class AdminBerandaFragment : Fragment() {

    private var _binding: FragmentAdminBerandaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminDashboardViewModel by viewModels {
        AdminDashboardViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupActions()
        observeViewModel()

        // Update online status and fetch admin profile
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.let { user ->
            viewModel.updateOnlineStatus(user.uid)
            fetchAdminProfile(user.uid)
        }
    }

    private fun fetchAdminProfile(uid: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("admins").document(uid).get()
            .addOnSuccessListener { doc ->
                if (isAdded) {
                    val name = if (doc.exists()) doc.getString("name") ?: "Admin" else "Admin"
                    binding.tvAdminRoleName.text = name
                    
                    // Set avatar initial from name (e.g., "Abdin Mujid" -> "AM")
                    val initials = name.split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .map { it[0].uppercase() }
                        .joinToString("")
                    
                    binding.tvAvatarInitial.text = if (initials.isNotEmpty()) initials else "AD"
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    binding.tvAdminRoleName.text = "Admin Pengelola"
                    binding.tvAvatarInitial.text = "AD"
                }
            }
    }

    private fun setupUI() {
        with(binding) {
            summaryTanaman.apply {
                ivSummaryIcon.setImageResource(R.drawable.herbal)
                tvSummaryTitle.text = "Total Tanaman"
            }
            
            summaryResep.apply {
                ivSummaryIcon.setImageResource(R.drawable.icoon_book)
                ivSummaryIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.admin_blue)
                tvSummaryTitle.text = "Total Resep"
                tvSummaryTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.admin_blue))
            }
            
            summarySurvei.apply {
                ivSummaryIcon.setImageResource(R.drawable.basil_edit_outline)
                ivSummaryIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.admin_orange)
                tvSummaryValue.text = "-"
                tvSummaryTitle.text = "Daftar Masukan"
                tvSummaryTrend.text = "Perlu ditinjau"
                tvSummaryTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.admin_orange))
            }
            
            summaryPengelola.apply {
                ivSummaryIcon.setImageResource(R.drawable.ic_person)
                ivSummaryIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.admin_red)
                tvSummaryTitle.text = "Pengelola Aktif"
                tvSummaryTrend.setTextColor(ContextCompat.getColor(requireContext(), R.color.admin_red))
            }

            menuTanaman.apply {
                ivMenuIcon.setImageResource(R.drawable.herbal)
                tvMenuLabel.text = "Tanaman"
            }
            
            menuResep.apply {
                ivMenuIcon.setImageResource(R.drawable.icoon_book)
                ivMenuIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.admin_blue)
                tvMenuLabel.text = "Resep"
            }
            
            menuSurvei.apply {
                ivMenuIcon.setImageResource(R.drawable.basil_edit_outline)
                ivMenuIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.admin_orange)
                tvMenuLabel.text = "Masukan"
            }
            
            menuLaporan.apply {
                ivMenuIcon.setImageResource(R.drawable.chart)
                ivMenuIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.admin_teal)
                tvMenuLabel.text = "Laporan"
            }
            
            menuBantuan.apply {
                ivMenuIcon.setImageResource(R.drawable.help)
                ivMenuIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.hijau_topakuli)
                tvMenuLabel.text = "Bantuan"
            }

            activity1.apply {
                tvActivityInitial.text = "AM"
                tvActivityText.text = "Abdin menambah tanaman"
                tvActivitySubtext.text = "10 menit lalu"
            }
            
            activity2.apply {
                tvActivityInitial.text = "SR"
                tvActivityInitial.setTextColor(ContextCompat.getColor(requireContext(), R.color.admin_blue))
                tvActivityText.text = "Sari mengubah resep"
                tvActivitySubtext.text = "1 jam lalu"
            }
            
            activity3.apply {
                tvActivityInitial.text = "RD"
                tvActivityInitial.setTextColor(ContextCompat.getColor(requireContext(), R.color.admin_orange))
                tvActivityText.text = "Rudi menambah data"
                tvActivitySubtext.text = "2 jam lalu"
            }

            plantStat1.apply { tvStatsName.text = "Mayana"; tvStatsCount.text = "201x"; progressStats.progress = 90 }
            plantStat2.apply { tvStatsName.text = "Meniran"; tvStatsCount.text = "162x"; progressStats.progress = 75 }
            plantStat3.apply { tvStatsName.text = "Kumis Kucing"; tvStatsCount.text = "98x"; progressStats.progress = 45 }

            recipeStat1.root.visibility = View.GONE
            recipeStat2.root.visibility = View.GONE
            recipeStat3.root.visibility = View.GONE

            // Visitor stats will be updated from ViewModel
        }
    }

    private fun setupActions() {
        binding.menuTanaman.root.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.admin_nav_view)
            bottomNav.selectedItemId = R.id.admin_navigation_herbal
        }
        binding.menuResep.root.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.admin_nav_view)
            bottomNav.selectedItemId = R.id.admin_navigation_resep
        }
        binding.menuSurvei.root.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.admin_nav_view)
            bottomNav.selectedItemId = R.id.admin_navigation_survei
        }
        binding.menuBantuan.root.setOnClickListener {
            findNavController().navigate(R.id.admin_navigation_help)
        }
        
        binding.menuLaporan.root.setOnClickListener {
            findNavController().navigate(R.id.action_admin_navigation_beranda_to_adminLaporanFragment)
        }
        
        binding.tvLihatSemuaActivity.setOnClickListener {
            findNavController().navigate(R.id.action_admin_navigation_beranda_to_adminActivityListFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tanamanSummary.collect { data ->
                        binding.summaryTanaman.apply {
                            tvSummaryValue.text = data.total.toString()
                            tvSummaryTrend.text = "+ ${data.thisMonth} bulan ini"
                        }
                    }
                }
                launch {
                    viewModel.resepSummary.collect { data ->
                        binding.summaryResep.apply {
                            tvSummaryValue.text = data.total.toString()
                            tvSummaryTrend.text = "+ ${data.thisMonth} bulan ini"
                        }
                    }
                }
                launch {
                    viewModel.adminSummary.collect { (total, online) ->
                        binding.summaryPengelola.apply {
                            tvSummaryValue.text = total.toString()
                            tvSummaryTrend.text = "$online online"
                        }
                    }
                }
                launch {
                    viewModel.masukanSummary.collect { data ->
                        binding.summarySurvei.apply {
                            tvSummaryValue.text = data.total.toString() // Angka besar: TOTAL masukan
                            tvSummaryTrend.text = if (data.thisMonth > 0) "${data.thisMonth} belum dibaca" else "Semua sudah dibaca"
                            tvSummaryTrend.setTextColor(if (data.thisMonth > 0) ContextCompat.getColor(requireContext(), R.color.admin_red) else ContextCompat.getColor(requireContext(), R.color.admin_orange))
                        }
                    }
                }
                launch {
                    viewModel.recentActivities.collect { logs ->
                        updateActivitiesUI(logs)
                    }
                }
                launch {
                    viewModel.popularResepStats.collect { list ->
                        updatePopularResepUI(list)
                    }
                }
                launch {
                    viewModel.visitorSummary.collect { (pakuli, outside) ->
                        updateVisitorStatsUI(pakuli, outside)
                    }
                }
            }
        }
    }

    private fun updateVisitorStatsUI(pakuli: Int, outside: Int) {
        val total = (pakuli + outside).coerceAtLeast(1)
        
        binding.visitorStat1.apply {
            tvStatsName.text = "Masyarakat Desa Pakuli"
            tvStatsCount.text = pakuli.toString()
            progressStats.progress = (pakuli * 100 / total)
            progressStats.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_stats_orange)
        }
        
        binding.visitorStat2.apply {
            tvStatsName.text = "Masyarakat Luar Desa Pakuli"
            tvStatsCount.text = outside.toString()
            progressStats.progress = (outside * 100 / total)
            progressStats.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_stats_orange)
        }
    }

    private fun updatePopularResepUI(list: List<com.project.topakuli.data.local.entity.ResepEntity>) {
        val views = listOf(binding.recipeStat1, binding.recipeStat2, binding.recipeStat3)
        
        // Hide all first
        views.forEach { it.root.visibility = View.GONE }
        
        if (list.isEmpty()) return

        val maxViews = list.first().viewCount.coerceAtLeast(1)
        
        list.take(3).forEachIndexed { index, resep ->
            views[index].apply {
                root.visibility = View.VISIBLE
                tvStatsName.text = resep.judul
                tvStatsCount.text = "${resep.viewCount}x"
                
                // Calculate progress percentage relative to the top recipe
                progressStats.progress = (resep.viewCount * 100 / maxViews)
                progressStats.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_stats_blue)
            }
        }
    }

    private fun updateActivitiesUI(logs: List<com.project.topakuli.data.local.entity.ActivityLog>) {
        val views = listOf(binding.activity1, binding.activity2, binding.activity3)
        
        // Hide all first
        views.forEach { it.root.visibility = View.GONE }
        
        logs.take(3).forEachIndexed { index, log ->
            views[index].apply {
                root.visibility = View.VISIBLE
                
                // Set initial
                val initial = if (log.adminName.isNotEmpty()) {
                    log.adminName.split(" ").filter { it.isNotEmpty() }.take(2)
                        .map { it[0].uppercase() }.joinToString("")
                } else "AD"
                tvActivityInitial.text = initial
                
                // Set color based on type
                val colorRes = when (log.type) {
                    "Tambah" -> R.color.hijau_topakuli
                    "Ubah" -> R.color.admin_blue
                    "Hapus" -> R.color.admin_red
                    else -> R.color.admin_orange
                }
                tvActivityInitial.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
                
                // Set text
                tvActivityText.text = "${log.adminName} ${log.type.lowercase()} ${log.target.lowercase()} ${log.targetName}"
                
                // Set time
                tvActivitySubtext.text = formatTimeAgo(log.timestamp)
            }
        }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000 -> "Baru saja"
            diff < 3600_000 -> "${diff / 60_000} menit lalu"
            diff < 86400_000 -> "${diff / 3600_000} jam lalu"
            else -> "${diff / 86400_000} hari lalu"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
