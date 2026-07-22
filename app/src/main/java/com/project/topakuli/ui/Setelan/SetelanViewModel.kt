package com.project.topakuli.ui.Setelan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.launch

class SetelanViewModel(private val repository: TopakuliRepository) : ViewModel() {

    val tanamanCount = repository.tanamanCount.asLiveData()
    val resepCount = repository.resepCount.asLiveData()
    val historyCount = repository.historyCount.asLiveData()

    private val _adminCount = androidx.lifecycle.MutableLiveData<Int>()
    val adminCount: androidx.lifecycle.LiveData<Int> = _adminCount

    init {
        fetchAdminCount()
    }

    private fun fetchAdminCount() {
        viewModelScope.launch {
            try {
                val stats = repository.getAdminStats()
                _adminCount.postValue(stats.first) // stats.first is the total number of admins
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class Factory(private val repository: TopakuliRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SetelanViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SetelanViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
