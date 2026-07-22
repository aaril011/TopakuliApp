package com.project.topakuli.ui.admin.beranda

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.databinding.FragmentAdminLaporanBinding
import com.project.topakuli.databinding.ItemChartBarBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class AdminLaporanFragment : Fragment() {

    private var _binding: FragmentAdminLaporanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminLaporanViewModel by viewModels {
        AdminLaporanViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        updateMonthDisplay()

        binding.btnSelectMonth.setOnClickListener {
            showMonthYearPicker()
        }
    }

    private fun updateMonthDisplay() {
        val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        binding.tvSelectedMonth.text = "${monthNames[selectedMonth]} $selectedYear"
    }

    private fun showMonthYearPicker() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.picker_month)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.picker_year)

        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = monthNames
        monthPicker.value = selectedMonth

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = currentYear - 5
        yearPicker.maxValue = currentYear + 5
        yearPicker.value = selectedYear

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Periode Laporan")
            .setView(dialogView)
            .setPositiveButton("Terapkan") { _, _ ->
                selectedMonth = monthPicker.value
                selectedYear = yearPicker.value
                updateMonthDisplay()
                viewModel.fetchLaporanData(selectedMonth, selectedYear)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.laporanData.collect { data ->
                    updateSummaryCards(data)
                    updateChart(data.categoryStats)
                    updatePopularItemsUI(data)
                    updateUsageStatsUI(data)
                }
            }
        }
    }

    private fun updateUsageStatsUI(data: AdminLaporanViewModel.LaporanData) {
        // Visitor Section
        val totalVisitor = (data.visitorsPakuli + data.visitorsOutside).coerceAtLeast(1)
        com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportUsageVisitor1.root).apply {
            tvStatsName.text = "Masyarakat Desa Pakuli"
            tvStatsCount.text = data.visitorsPakuli.toString()
            progressStats.progress = (data.visitorsPakuli * 100 / totalVisitor)
            progressStats.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_stats_orange)
        }
        com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportUsageVisitor2.root).apply {
            tvStatsName.text = "Masyarakat Luar Desa Pakuli"
            tvStatsCount.text = data.visitorsOutside.toString()
            progressStats.progress = (data.visitorsOutside * 100 / totalVisitor)
            progressStats.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_stats_orange)
        }

        // Identification Section
        val identViews = listOf(
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportUsageIdent1.root),
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportUsageIdent2.root),
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportUsageIdent3.root)
        )
        identViews.forEach { it.root.visibility = View.GONE }
        if (data.topIdentified.isNotEmpty()) {
            val maxIdent = data.topIdentified.first().second.coerceAtLeast(1)
            data.topIdentified.forEachIndexed { index, pair ->
                if (index < identViews.size) {
                    identViews[index].apply {
                        root.visibility = View.VISIBLE
                        tvStatsName.text = pair.first
                        tvStatsCount.text = "${pair.second}x"
                        progressStats.progress = (pair.second * 100 / maxIdent)
                        progressStats.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_stats_green)
                    }
                }
            }
        }

        // Views Section
        val viewViews = listOf(
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportUsageView1.root),
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportUsageView2.root),
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportUsageView3.root)
        )
        viewViews.forEach { it.root.visibility = View.GONE }
        if (data.topViewed.isNotEmpty()) {
            val maxView = data.topViewed.first().second.coerceAtLeast(1)
            data.topViewed.forEachIndexed { index, pair ->
                if (index < viewViews.size) {
                    viewViews[index].apply {
                        root.visibility = View.VISIBLE
                        tvStatsName.text = pair.first
                        tvStatsCount.text = "${pair.second} tayangan"
                        progressStats.progress = (pair.second * 100 / maxView)
                        progressStats.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_stats_blue)
                    }
                }
            }
        }
    }

    private fun updatePopularItemsUI(data: AdminLaporanViewModel.LaporanData) {
        // Plants Section
        val plantViews = listOf(
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportPlantStat1.root),
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportPlantStat2.root),
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportPlantStat3.root)
        )
        
        plantViews.forEach { it.root.visibility = View.GONE }
        
        if (data.topPlants.isEmpty()) {
            binding.tvEmptyPlantReport.visibility = View.VISIBLE
        } else {
            binding.tvEmptyPlantReport.visibility = View.GONE
            val maxPlant = data.topPlants.first().second.coerceAtLeast(1)
            data.topPlants.take(3).forEachIndexed { index, pair ->
                plantViews[index].apply {
                    root.visibility = View.VISIBLE
                    tvStatsName.text = pair.first
                    tvStatsCount.text = "${pair.second} aktivitas"
                    progressStats.progress = (pair.second * 100 / maxPlant)
                    progressStats.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_stats_green)
                }
            }
        }

        // Recipes Section
        val recipeViews = listOf(
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportRecipeStat1.root),
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportRecipeStat2.root),
            com.project.topakuli.databinding.ItemStatsProgressBinding.bind(binding.reportRecipeStat3.root)
        )
        
        recipeViews.forEach { it.root.visibility = View.GONE }

        if (data.topRecipes.isEmpty()) {
            binding.tvEmptyRecipeReport.visibility = View.VISIBLE
        } else {
            binding.tvEmptyRecipeReport.visibility = View.GONE
            val maxRecipe = data.topRecipes.first().second.coerceAtLeast(1)
            data.topRecipes.take(3).forEachIndexed { index, pair ->
                recipeViews[index].apply {
                    root.visibility = View.VISIBLE
                    tvStatsName.text = pair.first
                    tvStatsCount.text = "${pair.second} aktivitas"
                    progressStats.progress = (pair.second * 100 / maxRecipe)
                    progressStats.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_progress_stats_blue)
                }
            }
        }
    }

    private fun updateSummaryCards(data: AdminLaporanViewModel.LaporanData) {
        binding.tvCountTanaman.text = data.tanamanBaru.toString()
        binding.tvDeletedTanaman.text = "${data.tanamanDihapus} dihapus"

        binding.tvCountResep.text = data.resepBaru.toString()
        binding.tvDeletedResep.text = "${data.resepDihapus} dihapus"

        binding.tvCountKoreksi.text = data.koreksiMasuk.toString()
        binding.tvDeletedKoreksi.text = "${data.koreksiDihapus} dihapus"

        binding.tvCountPengajuan.text = data.pengajuanData.toString()
        binding.tvDeletedPengajuan.text = "${data.pengajuanDihapus} dihapus"

        binding.tvCountWargaPakuli.text = data.visitorsPakuli.toString()
        binding.tvCountWargaLuar.text = data.visitorsOutside.toString()
    }

    private fun updateChart(categoryData: List<AdminLaporanViewModel.CategoryActivity>) {
        val barBindings = listOf(
            ItemChartBarBinding.bind(binding.bar1.root),
            ItemChartBarBinding.bind(binding.bar2.root),
            ItemChartBarBinding.bind(binding.bar3.root),
            ItemChartBarBinding.bind(binding.bar4.root)
        )

        categoryData.forEachIndexed { index, data ->
            if (index < barBindings.size) {
                val barBinding = barBindings[index]
                barBinding.tvMonthLabel.text = data.label
                barBinding.tvMonthLabel.textSize = 10f
                barBinding.tvMonthLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.navbar_item_inactive))
                
                barBinding.tvBarValue.text = data.total.toString()
                
                val params = barBinding.viewBar.layoutParams as LinearLayout.LayoutParams
                // Hitung tinggi berdasarkan area 0dp weight 1 di atas (sekitar 160dp total)
                val maxHeightPx = (140 * resources.displayMetrics.density).toInt()
                val targetHeight = (maxHeightPx * data.intensity).toInt().coerceAtLeast(15) // min 15px
                
                params.height = targetHeight
                barBinding.viewBar.layoutParams = params

                barBinding.viewBar.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), data.colorRes)
                )
                
                // Add value text on top of bar if we want, or just rely on cards
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
