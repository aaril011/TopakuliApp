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
