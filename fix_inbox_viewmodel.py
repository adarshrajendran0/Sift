import re

with open('app/src/main/java/com/adarsh/sift/InboxViewModel.kt', 'r') as f:
    content = f.read()

new_functions = """
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
"""

if "fun markAsDone" not in content:
    content = content.replace("fun deleteItem(item: Item) {", new_functions + "\n    fun deleteItem(item: Item) {")

with open('app/src/main/java/com/adarsh/sift/InboxViewModel.kt', 'w') as f:
    f.write(content)
