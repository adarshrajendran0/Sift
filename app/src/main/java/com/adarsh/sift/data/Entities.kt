package com.adarsh.sift.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(
    tableName = "items",
    indices = [
        androidx.room.Index("status"),
        androidx.room.Index("createdAt")
    ]
)
data class Item(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val rawText: String,
    val actionText: String? = null,
    val status: Status = Status.INBOX,
    val contextId: String? = null,
    val projectId: String? = null,
    val timeEstimate: TimeEstimate? = null,
    val dueAt: Instant? = null,
    val delegateTo: String? = null,
    val createdAt: Instant = Instant.now(),
    val processedAt: Instant? = null,
    val completedAt: Instant? = null,
    val skipCount: Int = 0,
    val source: Source
)

enum class Status { INBOX, NEXT_ACTION, PROJECT, WAITING_FOR, CALENDAR,
                    SOMEDAY, REFERENCE, DONE, TRASHED }
enum class TimeEstimate { TWO_MIN, SHORT, MEDIUM, LONG }
enum class Source { WIDGET, TILE, SHARE, PROCESS_TEXT, NOTIFICATION, VOICE, IN_APP }

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val fromStatus: Status? = null,
    val toStatus: Status,
    val oldActionText: String? = null,
    val oldContextId: String? = null,
    val oldProjectId: String? = null,
    val oldDueAt: Instant? = null,
    val oldDelegateTo: String? = null,
    val oldProcessedAt: Instant? = null,
    val at: Instant = Instant.now()
)

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null
)

@Entity(tableName = "contexts")
data class Context(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val label: String,
    val isDefault: Boolean = false
)
