package com.adarsh.sift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adarsh.sift.data.Context
import com.adarsh.sift.data.Item
import com.adarsh.sift.data.ItemRepository
import com.adarsh.sift.domain.Answer
import com.adarsh.sift.domain.ProcessingStateMachine
import com.adarsh.sift.domain.Step
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class ProcessSessionState(
    val isLoading: Boolean = true,
    val queue: List<Item> = emptyList(),
    val currentIndex: Int = 0,
    val processedCount: Int = 0,
    val doneOnSpotCount: Int = 0,
    val currentStep: Step = Step.Actionable,
    val isComplete: Boolean = false,
    
    // User edits
    val actionText: String = "",
    val projectName: String = "",
    val delegateTo: String = "",
    val dueAt: Instant? = null,
    val selectedContextId: String? = null,
    
    // Contexts loaded from DB
    val contexts: List<Context> = emptyList(),
    
    // For snackbar undo
    val lastResolvedItem: Item? = null,
    val lastResolvedStatus: com.adarsh.sift.data.Status? = null
)

class ProcessViewModel(
    private val repository: ItemRepository,
    private val appScope: kotlinx.coroutines.CoroutineScope,
    private val singleItemId: String? = null,
    private val haptics: HapticsService
) : ViewModel() {

    private val _state = MutableStateFlow(ProcessSessionState())
    val state: StateFlow<ProcessSessionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val snapshot = if (singleItemId != null) {
                val item = repository.getItemById(singleItemId)
                if (item != null) listOf(item) else emptyList()
            } else {
                repository.inboxSnapshot()
            }
            val contexts = repository.getAllContextsSync()
            _state.value = _state.value.copy(
                isLoading = false,
                queue = snapshot,
                contexts = contexts,
                isComplete = snapshot.isEmpty()
            )
            if (snapshot.isNotEmpty()) {
                setupCurrentItem()
            }
        }
    }

    private fun setupCurrentItem() {
        val st = _state.value
        val item = st.queue.getOrNull(st.currentIndex)
        if (item == null) {
            _state.value = st.copy(isComplete = true)
            return
        }

        val initialStep = if (item.skipCount >= 2) Step.StalledNudge else Step.Actionable
        _state.value = st.copy(
            currentStep = initialStep,
            actionText = item.rawText,
            projectName = "",
            delegateTo = "",
            dueAt = null,
            selectedContextId = null
        )
    }

    fun updateActionText(text: String) {
        _state.value = _state.value.copy(actionText = text)
    }
    
    fun updateProjectName(name: String) {
        _state.value = _state.value.copy(projectName = name)
    }
    
    fun updateDelegateTo(name: String) {
        _state.value = _state.value.copy(delegateTo = name)
    }
    
    fun updateDueAt(time: Instant?) {
        _state.value = _state.value.copy(dueAt = time)
    }
    
    fun updateContextId(id: String) {
        _state.value = _state.value.copy(selectedContextId = id)
    }

    fun answer(answer: Answer) {
        val st = _state.value
        val item = st.queue.getOrNull(st.currentIndex) ?: return

        val nextStep = ProcessingStateMachine.transition(st.currentStep, answer)
        
        if (st.currentStep == Step.StalledNudge && answer == Answer.KeepTrying) {
            appScope.launch {
                repository.resetSkipCount(item.id)
            }
            val newQueue = st.queue.toMutableList()
            newQueue[st.currentIndex] = item.copy(skipCount = 0)
            _state.value = st.copy(queue = newQueue, currentStep = nextStep)
            return
        }
        
        if (nextStep is Step.Resolved) {
            val isDoneOnSpot = nextStep.status == com.adarsh.sift.data.Status.DONE
            
            when {
                isDoneOnSpot -> haptics.fire(Feel.DoneOnSpot)
                nextStep.status == com.adarsh.sift.data.Status.SOMEDAY && st.currentStep == Step.StalledNudge -> haptics.fire(Feel.Parked)
                else -> haptics.fire(Feel.Filed)
            }
            
            _state.value = st.copy(
                lastResolvedItem = item,
                lastResolvedStatus = nextStep.status,
                processedCount = st.processedCount + 1,
                doneOnSpotCount = st.doneOnSpotCount + (if (isDoneOnSpot) 1 else 0)
            )
            
            // Perform resolution in app scope
            val actText = if (st.actionText.isNotBlank()) st.actionText else item.rawText
            val prjName = if (st.projectName.isNotBlank()) st.projectName else null
            
            appScope.launch {
                repository.resolveItem(
                    item = item,
                    status = nextStep.status,
                    actionText = actText,
                    contextId = st.selectedContextId,
                    dueAt = st.dueAt,
                    delegateTo = if (st.delegateTo.isNotBlank()) st.delegateTo else null,
                    projectName = prjName
                )
            }
            advanceQueue()
        } else {
            haptics.fire(Feel.Decided)
            _state.value = st.copy(currentStep = nextStep)
        }
    }

    fun skipCurrent() {
        val st = _state.value
        val item = st.queue.getOrNull(st.currentIndex) ?: return
        
        // In app scope so skip updates count
        appScope.launch {
            repository.skipItem(item)
        }
        
        // Move item to back of queue internally
        val newQueue = st.queue.toMutableList()
        val removed = newQueue.removeAt(st.currentIndex)
        newQueue.add(removed.copy(skipCount = removed.skipCount + 1))
        
        _state.value = st.copy(queue = newQueue)
        setupCurrentItem()
    }

    fun undoLast() {
        val st = _state.value
        val item = st.lastResolvedItem ?: return
        val status = st.lastResolvedStatus
        val wasDoneOnSpot = status == com.adarsh.sift.data.Status.DONE
        haptics.fire(Feel.Undone)
        appScope.launch {
            repository.undoLastEvent(item.id)
        }
        _state.value = st.copy(
            lastResolvedItem = null,
            lastResolvedStatus = null,
            processedCount = kotlin.math.max(0, st.processedCount - 1),
            doneOnSpotCount = if (wasDoneOnSpot) kotlin.math.max(0, st.doneOnSpotCount - 1) else st.doneOnSpotCount
        )
    }

    private fun advanceQueue() {
        val st = _state.value
        val nextIdx = st.currentIndex + 1
        if (nextIdx >= st.queue.size) {
            _state.value = st.copy(currentIndex = nextIdx, isComplete = true)
        } else {
            _state.value = st.copy(currentIndex = nextIdx)
            setupCurrentItem()
        }
    }
}

