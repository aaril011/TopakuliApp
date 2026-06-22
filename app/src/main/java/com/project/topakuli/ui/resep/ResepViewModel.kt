package com.project.topakuli.ui.resep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ResepViewModel(private val repository: TopakuliRepository) : ViewModel() {

    enum class FilterMode { ALL, SAVED, HISTORY }

    private val _searchQuery = MutableStateFlow("")
    private val _filterMode = MutableStateFlow(FilterMode.ALL)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    val allResep: StateFlow<List<ResepEntity>> = repository.allResep
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val resepPaging: Flow<PagingData<ResepEntity>> = combine(_searchQuery, _filterMode) { query, mode ->
        query to mode
    }.flatMapLatest { (query, mode) ->
        when (mode) {
            FilterMode.ALL -> repository.getResepPaging(query, false)
            FilterMode.SAVED -> repository.getResepPaging(query, true)
            FilterMode.HISTORY -> repository.getResepHistoryPaging(query)
        }
    }.cachedIn(viewModelScope)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterMode(mode: FilterMode) {
        _filterMode.value = mode
    }

    fun resetState() {
        _searchQuery.value = ""
        _filterMode.value = FilterMode.ALL
    }

    init {
        syncData()
    }

    fun syncData() {
        viewModelScope.launch {
            repository.syncResep()
        }
    }

    fun toggleSave(resep: ResepEntity) {
        viewModelScope.launch {
            repository.toggleSaveResep(resep)
        }
    }

    fun removeFromHistory(resep: ResepEntity) {
        viewModelScope.launch {
            repository.removeFromHistory(resep.id)
        }
    }

    class Factory(private val repository: TopakuliRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResepViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ResepViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
