package com.project.topakuli.ui.admin.beranda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.data.local.entity.ActivityLog
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class AdminDashboardViewModel(private val repository: TopakuliRepository) : ViewModel() {

    private val _tanamanSearchQuery = MutableStateFlow("")
    private val _resepSearchQuery = MutableStateFlow("")

    private val _adminSummary = MutableStateFlow<Pair<Int, Int>>(Pair(0, 0))
    val adminSummary: StateFlow<Pair<Int, Int>> = _adminSummary.asStateFlow()

    private val _visitorSummary = MutableStateFlow<Pair<Int, Int>>(Pair(0, 0))
    val visitorSummary: StateFlow<Pair<Int, Int>> = _visitorSummary.asStateFlow()

    private val _masukanSummary = MutableStateFlow<SummaryData>(SummaryData(0, 0))
    val masukanSummary: StateFlow<SummaryData> = _masukanSummary.asStateFlow()

    private val _recentActivities = MutableStateFlow<List<ActivityLog>>(emptyList())
    val recentActivities: StateFlow<List<ActivityLog>> = _recentActivities.asStateFlow()

    val popularResepStats: StateFlow<List<ResepEntity>> = repository.popularResep
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tanamanPaging: StateFlow<PagingData<TanamanObatEntity>> = _tanamanSearchQuery
        .flatMapLatest { query ->
            repository.getTanamanPaging(query, false)
        }
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    @OptIn(ExperimentalCoroutinesApi::class)
    val resepPaging: StateFlow<PagingData<ResepEntity>> = _resepSearchQuery
        .flatMapLatest { query ->
            repository.getResepPaging(query, false)
        }
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

    fun searchTanaman(query: String) {
        _tanamanSearchQuery.value = query
    }

    fun searchResep(query: String) {
        _resepSearchQuery.value = query
    }

    init {
        syncData()
        fetchAdminStats()
        fetchRecentActivities()
        fetchVisitorStats()
    }

    private var isSynced = false

    fun syncData() {
        if (isSynced) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.syncTanaman()
                repository.syncResep()
                fetchAdminStats()
                fetchVisitorStats()
                fetchRecentActivities()
                fetchMasukanStats()
                isSynced = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchMasukanStats() {
        viewModelScope.launch {
            val contributions = repository.getUserContributions()
            val total = contributions.size
            val unread = contributions.count { !it.isReadByAdmin }
            _masukanSummary.value = SummaryData(total, unread)
        }
    }

    fun fetchAdminStats() {
        viewModelScope.launch {
            val stats = repository.getAdminStats()
            _adminSummary.value = stats
        }
    }

    fun fetchVisitorStats() {
        viewModelScope.launch {
            val stats = repository.getVisitorStats()
            _visitorSummary.value = stats
        }
    }

    fun fetchRecentActivities() {
        viewModelScope.launch {
            val logs = repository.getRecentActivities()
            _recentActivities.value = logs
        }
    }

    fun deleteActivityLog(id: String) {
        viewModelScope.launch {
            repository.deleteActivityLog(id)
            fetchRecentActivities() // Refresh list
        }
    }

    fun updateOnlineStatus(uid: String) {
        viewModelScope.launch {
            repository.updateAdminOnlineStatus(uid)
            fetchAdminStats()
            fetchRecentActivities()
        }
    }

    data class SummaryData(
        val total: Int,
        val thisMonth: Int
    )

    val tanamanSummary: StateFlow<SummaryData> = repository.allTanaman
        .map { list ->
            if (list.isEmpty()) return@map SummaryData(0, 0)
            val now = Calendar.getInstance()
            val currentMonth = now.get(Calendar.MONTH)
            val currentYear = now.get(Calendar.YEAR)
            
            SummaryData(
                total = list.size,
                thisMonth = list.count { 
                    val itemDate = Calendar.getInstance().apply { timeInMillis = it.createdAt }
                    itemDate.get(Calendar.YEAR) == currentYear && itemDate.get(Calendar.MONTH) == currentMonth
                }
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryData(0, 0))

    val resepSummary: StateFlow<SummaryData> = repository.allResep
        .map { list ->
            if (list.isEmpty()) return@map SummaryData(0, 0)
            val now = Calendar.getInstance()
            val currentMonth = now.get(Calendar.MONTH)
            val currentYear = now.get(Calendar.YEAR)

            SummaryData(
                total = list.size,
                thisMonth = list.count { 
                    val itemDate = Calendar.getInstance().apply { timeInMillis = it.createdAt }
                    itemDate.get(Calendar.YEAR) == currentYear && itemDate.get(Calendar.MONTH) == currentMonth
                }
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryData(0, 0))


    class Factory(private val repository: TopakuliRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminDashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminDashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
