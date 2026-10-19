package com.adarsh.sift

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adarsh.sift.data.AppDatabase
import com.adarsh.sift.data.ItemRepository
import com.adarsh.sift.data.Source
import com.adarsh.sift.data.Status
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: ItemRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ItemRepository(db.itemDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadItem() = runBlocking {
        repository.insertItem("buy milk", Source.IN_APP)

        val items = repository.getAllItems().first()
        assertEquals(1, items.size)
        
        val item = items[0]
        assertEquals("buy milk", item.rawText)
        assertEquals(Status.INBOX, item.status)

        val events = db.itemDao().getEventsForItem(item.id)
        assertEquals(1, events.size)
        val event = events[0]
        assertEquals(null, event.fromStatus)
        assertEquals(Status.INBOX, event.toStatus)
    }
}
