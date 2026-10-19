import re

with open('app/src/main/java/com/adarsh/sift/data/Entities.kt', 'r') as f:
    content = f.read()

old_event = """@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val fromStatus: Status? = null,
    val toStatus: Status,
    val at: Instant = Instant.now()
)"""

new_event = """@Entity(tableName = "events")
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
)"""

content = content.replace(old_event, new_event)
with open('app/src/main/java/com/adarsh/sift/data/Entities.kt', 'w') as f:
    f.write(content)
