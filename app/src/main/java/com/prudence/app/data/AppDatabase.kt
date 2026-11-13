package com.prudence.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.prudence.app.domain.Category

@Database(entities = [ExpenseEntity::class, CategoryEntity::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        /** v1 -> v2: add the category tree, seed it, remap skeleton-era expenses (categoryId 0) to Uncategorized. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categories` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`parentId` INTEGER)"
                )
                db.execSQL(CategorySeeds.INSERT_SQL.trimIndent())
                db.execSQL("UPDATE expenses SET categoryId = ${Category.UNCATEGORIZED_ID} WHERE categoryId = 0")
            }
        }
    }
}
