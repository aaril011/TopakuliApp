package com.project.topakuli.ui.notifikasi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.topakuli.data.local.entity.NotificationEntity
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotifikasiViewModel(private val repository: TopakuliRepository) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    fun fetchNotifications(sinceTimestamp: Long = 0L) {
        viewModelScope.launch {
            try {
                val remoteList = repository.getRemoteNotifications(sinceTimestamp)
                _notifications.value = remoteList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class Factory(private val repository: TopakuliRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotifikasiViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotifikasiViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
