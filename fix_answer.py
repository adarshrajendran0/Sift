import re

with open('app/src/main/java/com/adarsh/sift/ProcessViewModel.kt', 'r') as f:
    content = f.read()

old_answer = """    fun answer(answer: Answer) {
        val st = _state.value
        val item = st.queue.getOrNull(st.currentIndex) ?: return
        
        // Reset skip count if we kept trying
        val itemToProcess = if (st.currentStep == Step.StalledNudge && answer == Answer.KeepTrying) {
            item.copy(skipCount = 0)
        } else item

        val nextStep = ProcessingStateMachine.transition(st.currentStep, answer)
        
        if (nextStep is Step.Resolved) {
            val isDoneOnSpot = nextStep.status == com.adarsh.sift.data.Status.DONE
            
            _state.value = st.copy(
                lastResolvedItem = itemToProcess,
                lastResolvedStatus = nextStep.status,
                processedCount = st.processedCount + 1,
                doneOnSpotCount = st.doneOnSpotCount + (if (isDoneOnSpot) 1 else 0)
            )
            
            // Perform resolution in app scope
            val actText = if (st.actionText.isNotBlank()) st.actionText else itemToProcess.rawText
            val prjName = if (st.projectName.isNotBlank()) st.projectName else null
            
            appScope.launch {
                repository.resolveItem(
                    item = itemToProcess,
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
            _state.value = st.copy(currentStep = nextStep)
        }
    }"""

new_answer = """    fun answer(answer: Answer) {
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
            _state.value = st.copy(currentStep = nextStep)
        }
    }"""

content = content.replace(old_answer, new_answer)
with open('app/src/main/java/com/adarsh/sift/ProcessViewModel.kt', 'w') as f:
    f.write(content)
