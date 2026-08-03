package com.instasave.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasave.app.core.data.repository.DownloadHistoryRepository
import com.instasave.app.core.database.entity.DownloadEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: DownloadHistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL") // "ALL", "VIDEO", "PHOTO"
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val historyItems: StateFlow<List<DownloadEntity>> = combine(_searchQuery, _selectedFilter) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        repository.getDownloadHistory(query).map { list ->
            if (filter == "ALL") list else list.filter { it.mediaType.equals(filter, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}
