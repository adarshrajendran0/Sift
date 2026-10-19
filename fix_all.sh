#!/bin/bash
sed -i '/fun undoDelete/i \    fun addNextActionToProject(projectId: String, actionText: String, contextId: String?) {\n        viewModelScope.launch {\n            repository.addNextActionToProject(projectId, actionText, contextId)\n        }\n    }\n' app/src/main/java/com/adarsh/sift/InboxViewModel.kt

sed -i 's/verticalAlignment = Alignment.CenterVertically,/verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,/g' app/src/main/java/com/adarsh/sift/ListsScreen.kt
sed -i 's/^}$//g' app/src/main/java/com/adarsh/sift/ListsScreen.kt
