#!/bin/bash
sed -i '/fun untrashItem/i \    fun addNextActionToProject(projectId: String, actionText: String, contextId: String?) {\n        viewModelScope.launch {\n            repository.addNextActionToProject(projectId, actionText, contextId)\n        }\n    }\n' app/src/main/java/com/adarsh/sift/InboxViewModel.kt
