package com.project.topakuli.ui.notifikasi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.topakuli.data.local.entity.NotificationEntity
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotifikasiViewModel(private val repository: TopakuliRepository) : ViewModel() {

    val allNotifications: Flow<List<NotificationEntity>> = repository.allNotifications

    fun syncNotifications() {
        viewModelScope.launch {
            repository.syncNotifications()
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
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
