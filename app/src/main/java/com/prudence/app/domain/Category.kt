package com.prudence.app.domain

/** Domain type per GLOSSARY.md: a node in the user-editable tree that classifies expenses. */
data class Category(
    val id: Long,
    val name: String,
    val parentId: Long?,
) {
    companion object {
        /** The one permanent root category; undeletable, sink for root-category deletions. */
        const val UNCATEGORIZED_ID = 1L
    }
}

/** Ids of every descendant of [id] in this flat category list. Used by the cycle guard and the UI's move filter. */
fun List<Category>.descendantIdsOf(id: Long): Set<Long> {
    val childrenByParent = groupBy { it.parentId }
    val result = mutableSetOf<Long>()
    val queue = ArrayDeque(listOf(id))
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        childrenByParent[current].orEmpty().forEach { child ->
            if (result.add(child.id)) queue.addLast(child.id)
        }
    }
    return result
}

/** Path from the root down to [id] inclusive; empty when [id] is unknown. Drives the chip-path picker. */
fun List<Category>.pathTo(id: Long): List<Category> {
    val byId = associateBy { it.id }
    val path = mutableListOf<Category>()
    val visited = mutableSetOf<Long>()
    var current: Long? = id
    while (current != null && visited.add(current)) {
        val category = byId[current] ?: return emptyList()
        path.add(category)
        current = category.parentId
    }
    return path.asReversed()
}
