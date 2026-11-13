package com.prudence.app.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.prudence.app.domain.Category

/**
 * First-run seed per SPEC.md §2: flat defaults plus the permanent Uncategorized (fixed ids,
 * INSERT OR IGNORE so the same statement serves both fresh installs (onCreate) and the 1->2
 * migration without duplicating on conflict).
 */
object CategorySeeds {

    const val INSERT_SQL = """
        INSERT OR IGNORE INTO categories (id, name, parentId) VALUES
            (${Category.UNCATEGORIZED_ID}, 'Uncategorized', NULL),
            (2, 'Food', NULL),
            (3, 'Transport', NULL),
            (4, 'Housing', NULL),
            (5, 'Health', NULL),
            (6, 'Entertainment', NULL)
    """

    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            db.execSQL(INSERT_SQL.trimIndent())
        }
    }
}
