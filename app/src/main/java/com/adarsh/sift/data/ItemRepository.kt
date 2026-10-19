package com.adarsh.sift.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class ItemRepository(private val db: AppDatabase) {
    private val dao = db.itemDao()

    fun getAllItems(): Flow<List<Item>> = dao.getAllItems()
    suspend fun getItemById(id: String): Item? = dao.getItemById(id)
    suspend fun inboxSnapshot(): List<Item> = dao.inboxSnapshot()
    fun itemsByStatus(status: Status): Flow<List<Item>> = dao.itemsByStatus(status)
    fun getAllProjects(): Flow<List<Project>> = dao.getAllProjects()
    fun getOpenActionCountForProject(projectId: String): Flow<Int> = dao.getOpenActionCountForProject(projectId)
    fun getAllContexts(): Flow<List<Context>> = dao.getAllContexts()
    suspend fun getAllContextsSync(): List<Context> = dao.getAllContextsSync()

    suspend fun insertContext(label: String) {
        db.contextDao().insertContext(Context(label = label))
    }
    suspend fun updateContextLabel(id: String, label: String) {
        db.contextDao().updateContextLabel(id, label)
    }
    suspend fun deleteContext(id: String) {
        db.withTransaction {
            db.contextDao().clearItemsContext(id)
            db.contextDao().deleteContext(id)
        }
    }

    suspend fun insertItem(text: String, source: Source) {
        if (text.isBlank()) return
        val item = Item(rawText = text, source = source)
        val event = Event(itemId = item.id, fromStatus = null, toStatus = Status.INBOX)
        db.withTransaction {
            dao.insertItem(item)
            dao.insertEvent(event)
        }
    }
    
    suspend fun restoreItem(item: Item, event: Event? = null) {
        db.withTransaction {
            dao.insertItem(item)
            if (event != null) dao.insertEvent(event)
        }
    }

    suspend fun trashItem(id: String) {
        db.withTransaction {
            dao.updateItemStatus(id, Status.TRASHED)
            dao.insertEvent(Event(itemId = id, fromStatus = Status.INBOX, toStatus = Status.TRASHED))
        }
    }

    suspend fun untrashItem(id: String) {
        db.withTransaction {
            dao.updateItemStatus(id, Status.INBOX)
            dao.insertEvent(Event(itemId = id, fromStatus = Status.TRASHED, toStatus = Status.INBOX))
        }
    }

    suspend fun updateItemText(id: String, text: String) {
        dao.updateItemText(id, text)
    }
    
    suspend fun addNextActionToProject(projectId: String, actionText: String, contextId: String?) {
        val item = Item(
            rawText = actionText,
            actionText = actionText,
            status = Status.NEXT_ACTION,
            projectId = projectId,
            contextId = contextId,
            source = Source.IN_APP
        )
        val event = Event(
            itemId = item.id,
            fromStatus = null,
            toStatus = Status.NEXT_ACTION
        )
        db.withTransaction {
            dao.insertItem(item)
            dao.insertEvent(event)
        }
    }

    suspend fun resetSkipCount(id: String) {
        dao.resetSkipCount(id)
    }

    suspend fun deleteItem(id: String) {
        dao.deleteItem(id)
    }

    suspend fun skipItem(item: Item) {
        db.withTransaction {
            dao.insertItem(item.copy(skipCount = item.skipCount + 1))
        }
    }

    suspend fun resolveItem(
        item: Item,
        status: Status,
        actionText: String? = null,
        contextId: String? = null,
        dueAt: Instant? = null,
        delegateTo: String? = null,
        projectName: String? = null
    ) {
        db.withTransaction {
            var projectId: String? = null
            
            // If project was created, insert it
            if (projectName != null) {
                val project = Project(title = projectName)
                dao.insertProject(project)
                projectId = project.id
            }

            val updatedItem = item.copy(
                status = status,
                actionText = actionText ?: item.actionText,
                contextId = contextId,
                projectId = projectId ?: item.projectId,
                dueAt = dueAt,
                delegateTo = delegateTo,
                processedAt = Instant.now(),
                skipCount = 0 // resetting skip count on resolution just in case
            )

            val event = Event(
                itemId = item.id,
                fromStatus = item.status,
                toStatus = status,
                oldActionText = item.actionText,
                oldContextId = item.contextId,
                oldProjectId = item.projectId,
                oldDueAt = item.dueAt,
                oldDelegateTo = item.delegateTo,
                oldProcessedAt = item.processedAt
            )

            dao.insertItem(updatedItem)
            dao.insertEvent(event)
        }
    }

    suspend fun undoLastEvent(itemId: String) {
        db.withTransaction {
            val lastEvent = dao.getLastEventForItem(itemId)
            if (lastEvent != null && lastEvent.fromStatus != null) {
                val previousStatus = lastEvent.fromStatus
                val newEvent = Event(
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
                dao.insertEvent(newEvent)
            }
        }
    }
}
