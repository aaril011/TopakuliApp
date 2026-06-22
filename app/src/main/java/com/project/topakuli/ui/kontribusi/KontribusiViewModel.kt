package com.project.topakuli.ui.kontribusi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.project.topakuli.data.local.entity.ChatEntity
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class KontribusiViewModel(private val repository: TopakuliRepository) : ViewModel() {

    val allChats: Flow<List<ChatEntity>> = repository.allChats

    fun saveChat(chat: ChatEntity) {
        viewModelScope.launch {
            repository.insertChat(chat)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllChats()
        }
    }

    fun deleteChatByType(type: String) {
        viewModelScope.launch {
            repository.deleteChatByType(type)
        }
    }

    class Factory(private val repository: TopakuliRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(KontribusiViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return KontribusiViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
