package com.project.topakuli.ui.admin.beranda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class AdminLaporanViewModel(private val repository: TopakuliRepository) : ViewModel() {

    private val _laporanData = MutableStateFlow<LaporanData>(LaporanData())
    val laporanData: StateFlow<LaporanData> = _laporanData.asStateFlow()

    data class LaporanData(
        val tanamanBaru: Int = 0,
        val resepBaru: Int = 0,
        val koreksiMasuk: Int = 0,
        val pengajuanData: Int = 0,
        val tanamanDihapus: Int = 0,
        val resepDihapus: Int = 0,
        val koreksiDihapus: Int = 0,
        val pengajuanDihapus: Int = 0,
        val categoryStats: List<CategoryActivity> = emptyList()
    )

    data class CategoryActivity(
        val label: String,
        val total: Int,
        val intensity: Float,
        val colorRes: Int
    )

    init {
        val now = Calendar.getInstance()
        fetchLaporanData(now.get(Calendar.MONTH), now.get(Calendar.YEAR))
    }

    fun fetchLaporanData(month: Int, year: Int) {
        viewModelScope.launch {
            try {
                // Increase limit to 500 to ensure we get enough data for monthly statistics
                val logs = repository.getRecentActivities(500)
                
                // Ambil data kontribusi untuk statistik
                val contributions = repository.getAllContributions()

                // Calculate summaries for SELECTED MONTH
                val tanamanBaru = logs.count { 
                    it.type == "Tambah" && it.target == "Tanaman" && isTargetMonth(it.timestamp, month, year)
                }
                val resepBaru = logs.count { 
                    it.type == "Tambah" && it.target == "Resep" && isTargetMonth(it.timestamp, month, year)
                }
                
                // Hitung berdasarkan type di user_contributions
                val koreksiMasuk = contributions.count { 
                    it.type == "koreksi" && isTargetMonth(it.timestamp, month, year)
                }
                val pengajuanData = contributions.count { 
                    (it.type == "resep" || it.type == "tanaman") && isTargetMonth(it.timestamp, month, year)
                }

                // Deleted Counts (from logs)
                val tanamanDihapus = logs.count {
                    it.type == "Hapus" && it.target == "Tanaman" && isTargetMonth(it.timestamp, month, year)
                }
                val resepDihapus = logs.count {
                    it.type == "Hapus" && it.target == "Resep" && isTargetMonth(it.timestamp, month, year)
                }
                val koreksiDihapus = logs.count {
                    it.type == "Hapus" && it.target == "Koreksi" && isTargetMonth(it.timestamp, month, year)
                }
                val pengajuanDihapus = logs.count {
                    it.type == "Hapus" && it.target == "Ajuan" && isTargetMonth(it.timestamp, month, year)
                }

                // Prepare Category Stats for Chart
                val rawStats = listOf(
                    Triple("Tanaman", tanamanBaru, com.project.topakuli.R.color.hijau_topakuli),
                    Triple("Resep", resepBaru, com.project.topakuli.R.color.admin_blue),
                    Triple("Koreksi", koreksiMasuk, com.project.topakuli.R.color.admin_orange),
                    Triple("Pengajuan", pengajuanData, com.project.topakuli.R.color.admin_red)
                )

                val maxVal = rawStats.maxOf { it.second }.coerceAtLeast(1)
                val categoryStats = rawStats.map { (label, count, color) ->
                    CategoryActivity(label, count, count.toFloat() / maxVal, color)
                }

                _laporanData.value = LaporanData(
                    tanamanBaru, resepBaru, koreksiMasuk, pengajuanData,
                    tanamanDihapus, resepDihapus, koreksiDihapus, pengajuanDihapus,
                    categoryStats
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                android.util.Log.e("AdminLaporan", "Error fetching data", e)
            }
        }
    }

    private fun isTargetMonth(timestamp: Long, month: Int, year: Int): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
    }

    class Factory(private val repository: TopakuliRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminLaporanViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminLaporanViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
