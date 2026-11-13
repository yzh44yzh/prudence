package com.prudence.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    /** Seeds use fixed ids (1..6); user categories auto-generate from 7 up. */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Null = root. */
    val parentId: Long?,
)
