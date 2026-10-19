import re

with open('app/src/main/java/com/adarsh/sift/data/AppDatabase.kt', 'r') as f:
    content = f.read()

old_migration = """val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE events ADD COLUMN oldActionText TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN oldContextId TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN oldProjectId TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN oldDueAt INTEGER")
        db.execSQL("ALTER TABLE events ADD COLUMN oldDelegateTo TEXT")
        db.execSQL("ALTER TABLE events ADD COLUMN oldProcessedAt INTEGER")
    }
}"""

new_migration = """val MIGRATION_1_2 = object : Migration(1, 2) {
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
}"""

content = content.replace(old_migration, new_migration)

with open('app/src/main/java/com/adarsh/sift/data/AppDatabase.kt', 'w') as f:
    f.write(content)
