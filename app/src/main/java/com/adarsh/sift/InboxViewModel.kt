package com.adarsh.sift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adarsh.sift.data.Item
import com.adarsh.sift.data.ItemRepository
import com.adarsh.sift.data.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InboxViewModel(
    private val repository: ItemRepository
) : ViewModel() {

    val items = repository.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateItem(id: String, text: String) {
        viewModelScope.launch {
            repository.updateItemText(id, text)
        }
    }
    
    fun markAsDone(item: Item) {
        viewModelScope.launch {
            repository.resolveItem(item, status = com.adarsh.sift.data.Status.DONE)
        }
    }
    
    fun updateItemDetails(item: Item, actionText: String, contextId: String?) {
        viewModelScope.launch {
            repository.resolveItem(item, status = item.status, actionText = actionText, contextId = contextId)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.trashItem(item.id)
        }
    }

    fun addNextActionToProject(projectId: String, actionText: String, contextId: String?) {
        viewModelScope.launch {
            repository.addNextActionToProject(projectId, actionText, contextId)
        }
    }

    fun undoDelete(id: String) {
        viewModelScope.launch {
            repository.untrashItem(id)
        }
    }
    
    fun itemsByStatus(status: com.adarsh.sift.data.Status): kotlinx.coroutines.flow.Flow<List<com.adarsh.sift.data.Item>> = repository.itemsByStatus(status)
    val allProjects: kotlinx.coroutines.flow.Flow<List<com.adarsh.sift.data.Project>> = repository.getAllProjects()
    val allContexts: kotlinx.coroutines.flow.Flow<List<com.adarsh.sift.data.Context>> = repository.getAllContexts()
    fun openActionCount(projectId: String): kotlinx.coroutines.flow.Flow<Int> = repository.getOpenActionCountForProject(projectId)
    fun insertContext(label: String) = viewModelScope.launch { repository.insertContext(label) }
    fun updateContext(id: String, label: String) = viewModelScope.launch { repository.updateContextLabel(id, label) }
    fun deleteContext(id: String) = viewModelScope.launch { repository.deleteContext(id) }
}

class InboxViewModelFactory(private val repository: ItemRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InboxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InboxViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}