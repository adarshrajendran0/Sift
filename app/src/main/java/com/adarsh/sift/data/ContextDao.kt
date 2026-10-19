package com.adarsh.sift.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContextDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: Context)

    @Query("UPDATE contexts SET label = :label WHERE id = :id")
    suspend fun updateContextLabel(id: String, label: String)

    @Query("UPDATE items SET contextId = NULL WHERE contextId = :id")
    suspend fun clearItemsContext(id: String)

    @Query("DELETE FROM contexts WHERE id = :id")
    suspend fun deleteContext(id: String)
}
