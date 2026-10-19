package com.adarsh.sift.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Query("SELECT * FROM items WHERE status = 'INBOX' ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: String): Item?

    @Query("SELECT * FROM items WHERE status = 'INBOX' ORDER BY createdAt ASC")
    suspend fun inboxSnapshot(): List<Item>

    @Query("SELECT * FROM items WHERE status = :status ORDER BY createdAt DESC")
    fun itemsByStatus(status: Status): Flow<List<Item>>

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT COUNT(*) FROM items WHERE projectId = :projectId AND status IN ('NEXT_ACTION', 'WAITING_FOR', 'CALENDAR')")
    fun getOpenActionCountForProject(projectId: String): Flow<Int>
    
    @Query("SELECT * FROM contexts ORDER BY label ASC")
    fun getAllContexts(): Flow<List<Context>>
    
    @Query("SELECT * FROM contexts ORDER BY label ASC")
    suspend fun getAllContextsSync(): List<Context>

    @Query("SELECT * FROM events WHERE itemId = :itemId")
    suspend fun getEventsForItem(itemId: String): List<Event>

    @Query("SELECT * FROM events WHERE itemId = :itemId ORDER BY at DESC LIMIT 1")
    suspend fun getLastEventForItem(itemId: String): Event?

    @Query("UPDATE items SET rawText = :text WHERE id = :id")
    suspend fun updateItemText(id: String, text: String)

    @Query("UPDATE items SET status = :status WHERE id = :id")
    suspend fun updateItemStatus(id: String, status: Status)

    @Query("UPDATE items SET skipCount = 0 WHERE id = :id")
    suspend fun resetSkipCount(id: String)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItem(id: String)
}
