import re

with open('app/src/main/java/com/adarsh/sift/ProcessViewModel.kt', 'r') as f:
    content = f.read()

old_undo = """    fun undoLast() {
        val st = _state.value
        val item = st.lastResolvedItem ?: return
        appScope.launch {
            repository.undoLastEvent(item.id)
        }
        _state.value = st.copy(lastResolvedItem = null) // Only undo once
    }"""

new_undo = """    fun undoLast() {
        val st = _state.value
        val item = st.lastResolvedItem ?: return
        val status = st.lastResolvedStatus
        val wasDoneOnSpot = status == com.adarsh.sift.data.Status.DONE
        appScope.launch {
            repository.undoLastEvent(item.id)
        }
        _state.value = st.copy(
            lastResolvedItem = null,
            lastResolvedStatus = null,
            processedCount = kotlin.math.max(0, st.processedCount - 1),
            doneOnSpotCount = if (wasDoneOnSpot) kotlin.math.max(0, st.doneOnSpotCount - 1) else st.doneOnSpotCount
        )
    }"""

content = content.replace(old_undo, new_undo)
with open('app/src/main/java/com/adarsh/sift/ProcessViewModel.kt', 'w') as f:
    f.write(content)
