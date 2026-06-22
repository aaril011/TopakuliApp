package com.project.topakuli.ui.herbal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.project.topakuli.data.local.entity.TanamanObatEntity
import com.project.topakuli.data.repository.TopakuliRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HerbalViewModel(private val repository: TopakuliRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _onlyFavorites = MutableStateFlow(false)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    val allTanaman: StateFlow<List<TanamanObatEntity>> = repository.allTanaman
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tanamanPaging: Flow<PagingData<TanamanObatEntity>> = combine(_searchQuery, _onlyFavorites) { query, fav ->
        query to fav
    }.flatMapLatest { (query, fav) ->
        repository.getTanamanPaging(query, fav)
    }.cachedIn(viewModelScope)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setOnlyFavorites(onlyFavorites: Boolean) {
        _onlyFavorites.value = onlyFavorites
    }

    fun resetState() {
        _searchQuery.value = ""
        _onlyFavorites.value = false
    }

    init {
        syncData()
    }

    fun syncData() {
        viewModelScope.launch {
            repository.syncTanaman()
        }
    }

    fun toggleFavorit(tanaman: TanamanObatEntity) {
        viewModelScope.launch {
            repository.toggleFavorit(tanaman)
        }
    }

    class Factory(private val repository: TopakuliRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HerbalViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HerbalViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
