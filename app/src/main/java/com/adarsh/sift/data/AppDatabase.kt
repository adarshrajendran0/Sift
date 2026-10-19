package com.adarsh.sift.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE events ADD COLUMN oldActionText TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN oldContextId TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN oldProjectId TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN oldDueAt INTEGER")
        db.execSQL("ALTER TABLE events ADD COLUMN oldDelegateTo TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN oldProcessedAt INTEGER")
        db.execSQL("CREATE TABLE IF NOT EXISTS `projects` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `notes` TEXT, `createdAt` INTEGER NOT NULL, `completedAt` INTEGER, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `contexts` (`id` TEXT NOT NULL, `label` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, PRIMARY KEY(`id`))")
    }
}

val MIGRATION_2_1 = object : Migration(2, 1) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No-op
    }
}

val SeedContextsCallback = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val contexts = listOf("@phone", "@computer", "@home", "@errand", "@anywhere")
        contexts.forEach { label ->
            db.execSQL("INSERT INTO contexts (id, label, isDefault) VALUES ('${java.util.UUID.randomUUID()}', '$label', 0)")
        }
    }
}

@Database(
    entities = [Item::class, Event::class, Project::class, Context::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun contextDao(): ContextDao
}
