package com.adarsh.sift

import android.app.Application
import androidx.room.Room
import com.adarsh.sift.data.AppDatabase
import com.adarsh.sift.data.ItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers

class SiftApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "sift_db")
            .addMigrations(com.adarsh.sift.data.MIGRATION_2_1, com.adarsh.sift.data.MIGRATION_1_2)
            .addCallback(com.adarsh.sift.data.SeedContextsCallback)
            .build()
    }
    
    val repository by lazy {
        ItemRepository(database)
    }

    val haptics by lazy {
        HapticsService(this)
    }
}
