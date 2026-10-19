import re
with open('app/src/main/java/com/adarsh/sift/ProcessViewModel.kt', 'r') as f:
    content = f.read()

# Add haptics parameter to ProcessViewModel
content = content.replace('    private val singleItemId: String? = null', '    private val singleItemId: String? = null,\n    private val haptics: HapticsService')

# Trigger Filed, Parked, DoneOnSpot
resolved_block_old = """        if (nextStep is Step.Resolved) {
            val isDoneOnSpot = nextStep.status == com.adarsh.sift.data.Status.DONE
            
            _state.value = st.copy("""
resolved_block_new = """        if (nextStep is Step.Resolved) {
            val isDoneOnSpot = nextStep.status == com.adarsh.sift.data.Status.DONE
            
            when {
                isDoneOnSpot -> haptics.fire(Feel.DoneOnSpot)
                nextStep.status == com.adarsh.sift.data.Status.SOMEDAY && st.currentStep == Step.StalledNudge -> haptics.fire(Feel.Parked)
                else -> haptics.fire(Feel.Filed)
            }
            
            _state.value = st.copy("""
content = content.replace(resolved_block_old, resolved_block_new)

# Trigger Decided
else_block_old = """        } else {
            _state.value = st.copy(currentStep = nextStep)
        }"""
else_block_new = """        } else {
            haptics.fire(Feel.Decided)
            _state.value = st.copy(currentStep = nextStep)
        }"""
content = content.replace(else_block_old, else_block_new)

# Trigger Undone
undo_block_old = """        appScope.launch {
            repository.undoLastEvent(item.id)
        }"""
undo_block_new = """        haptics.fire(Feel.Undone)
        appScope.launch {
            repository.undoLastEvent(item.id)
        }"""
content = content.replace(undo_block_old, undo_block_new)

with open('app/src/main/java/com/adarsh/sift/ProcessViewModel.kt', 'w') as f:
    f.write(content)

# Now ProcessViewModelFactory
with open('app/src/main/java/com/adarsh/sift/ProcessViewModelFactory.kt', 'r') as f:
    factory = f.read()

factory = factory.replace('class ProcessViewModelFactory(', 'import com.adarsh.sift.HapticsService\n\nclass ProcessViewModelFactory(')
factory = factory.replace('    private val singleItemId: String? = null', '    private val singleItemId: String? = null,\n    private val haptics: HapticsService')
factory = factory.replace('return ProcessViewModel(repository, appScope, singleItemId) as T', 'return ProcessViewModel(repository, appScope, singleItemId, haptics) as T')

with open('app/src/main/java/com/adarsh/sift/ProcessViewModelFactory.kt', 'w') as f:
    f.write(factory)
