package com.project.topakuli.ui.beranda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BerandaViewModel(private val repository: TopakuliRepository) : ViewModel() {

    val allTanaman: StateFlow<List<TanamanObatEntity>> = repository.allTanaman
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allResep: StateFlow<List<ResepEntity>> = repository.allResep
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val randomTanaman: StateFlow<List<TanamanObatEntity>> = allTanaman
        .map { list -> if (list.isEmpty()) emptyList() else list.shuffled().take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val popularResep: StateFlow<List<ResepEntity>> = allResep
        .map { list -> if (list.isEmpty()) emptyList() else list.shuffled().take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<com.project.topakuli.data.local.entity.HistoryEntity>> = repository.allHistory
        .map { list -> list.take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncData()
    }

    fun syncData() {
        viewModelScope.launch {
            try {
                android.util.Log.d("TopakuliSync", "Memulai sinkronisasi data...")
                repository.syncTanaman()
                repository.syncResep()
                android.util.Log.d("TopakuliSync", "Sinkronisasi selesai")
            } catch (e: Exception) {
                android.util.Log.e("TopakuliSync", "Error saat sinkronisasi: ${e.message}")
            }
        }
    }

    fun submitVisitorOrigin(origin: String) {
        viewModelScope.launch {
            repository.incrementVisitorStats(origin)
        }
    }

    class Factory(private val repository: TopakuliRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BerandaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BerandaViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
