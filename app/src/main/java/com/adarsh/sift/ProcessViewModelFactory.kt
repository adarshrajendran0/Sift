package com.adarsh.sift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adarsh.sift.data.ItemRepository
import kotlinx.coroutines.CoroutineScope

import com.adarsh.sift.HapticsService

class ProcessViewModelFactory(
    private val repository: ItemRepository,
    private val appScope: CoroutineScope,
    private val singleItemId: String?,
    private val haptics: HapticsService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProcessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProcessViewModel(repository, appScope, singleItemId, haptics) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
