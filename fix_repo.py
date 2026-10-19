import re

with open('app/src/main/java/com/adarsh/sift/data/ItemRepository.kt', 'r') as f:
    content = f.read()

old_resolve = """            val event = Event(
                itemId = item.id,
                fromStatus = item.status,
                toStatus = status
            )"""

new_resolve = """            val event = Event(
                itemId = item.id,
                fromStatus = item.status,
                toStatus = status,
                oldActionText = item.actionText,
                oldContextId = item.contextId,
                oldProjectId = item.projectId,
                oldDueAt = item.dueAt,
                oldDelegateTo = item.delegateTo,
                oldProcessedAt = item.processedAt
            )"""

old_undo = """                val newEvent = Event(
                    itemId = itemId,
                    fromStatus = lastEvent.toStatus,
                    toStatus = previousStatus
                )
                dao.updateItemStatus(itemId, previousStatus)
                dao.insertEvent(newEvent)"""

new_undo = """                val newEvent = Event(
                    itemId = itemId,
                    fromStatus = lastEvent.toStatus,
                    toStatus = previousStatus
                )
                val item = dao.getItemById(itemId)
                if (item != null) {
                    val restoredItem = item.copy(
                        status = previousStatus,
                        actionText = lastEvent.oldActionText,
                        contextId = lastEvent.oldContextId,
                        projectId = lastEvent.oldProjectId,
                        dueAt = lastEvent.oldDueAt,
                        delegateTo = lastEvent.oldDelegateTo,
                        processedAt = lastEvent.oldProcessedAt
                    )
                    dao.insertItem(restoredItem)
                } else {
                    dao.updateItemStatus(itemId, previousStatus)
                }
                dao.insertEvent(newEvent)"""

content = content.replace(old_resolve, new_resolve)
content = content.replace(old_undo, new_undo)

with open('app/src/main/java/com/adarsh/sift/data/ItemRepository.kt', 'w') as f:
    f.write(content)
