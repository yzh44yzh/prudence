package com.prudence.app.data

import androidx.room.withTransaction
import com.prudence.app.domain.Category
import com.prudence.app.domain.descendantIdsOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(private val db: AppDatabase, private val dao: CategoryDao) {

    fun observeCategories(): Flow<List<Category>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    /** One-shot snapshot (e.g. for CSV export). */
    suspend fun getCategories(): List<Category> = dao.getAll().map { it.toDomain() }

    /** Null when the name is reserved (the permanent Uncategorized's name may not be reused). */
    suspend fun addCategory(name: String, parentId: Long?): Long? {
        if (isReservedName(name)) return null
        return dao.insert(CategoryEntity(name = name, parentId = parentId))
    }

    /** Uncategorized is permanent: it cannot be renamed, and its name may not be taken. */
    suspend fun renameCategory(id: Long, name: String): Boolean {
        if (id == Category.UNCATEGORIZED_ID || isReservedName(name)) return false
        dao.rename(id, name)
        return true
    }

    /**
     * Moves a category (with its whole subtree) under a new parent; null parent = root.
     * Rejected for Uncategorized, for a parent that doesn't exist, and for moves that
     * would create a cycle (under itself or one of its own descendants).
     */
    suspend fun moveCategory(id: Long, newParentId: Long?): Boolean {
        if (id == Category.UNCATEGORIZED_ID) return false
        val all = dao.getAll().map { it.toDomain() }
        if (newParentId != null) {
            if (all.none { it.id == newParentId }) return false
            if (newParentId in all.descendantIdsOf(id) + id) return false
        }
        dao.setParent(id, newParentId)
        return true
    }

    /**
     * Deletes a category without losing data (SPEC.md §2): its expenses are reassigned and its
     * child categories reparented to the deleted category's parent — or to Uncategorized when
     * a root is deleted. Uncategorized itself cannot be deleted.
     */
    suspend fun deleteCategory(id: Long): Boolean {
        if (id == Category.UNCATEGORIZED_ID) return false
        return db.withTransaction {
            val target = dao.getAll().firstOrNull { it.id == id } ?: return@withTransaction false
            val sink = target.parentId ?: Category.UNCATEGORIZED_ID
            dao.reassignExpenses(fromId = id, toId = sink)
            dao.reparentChildren(fromId = id, toId = sink)
            dao.deleteById(id)
            true
        }
    }

    private fun isReservedName(name: String) = name.equals("Uncategorized", ignoreCase = true)

    private fun CategoryEntity.toDomain() = Category(id = id, name = name, parentId = parentId)
}
