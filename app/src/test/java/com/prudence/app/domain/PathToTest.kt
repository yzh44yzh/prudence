package com.prudence.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PathToTest {

    private val tree = listOf(
        Category(Category.UNCATEGORIZED_ID, "Uncategorized", null),
        Category(2, "Food", null),
        Category(7, "Groceries", 2),
        Category(8, "Weekly", 7),
        Category(3, "Transport", null),
    )

    @Test
    fun `path from root to leaf`() {
        assertEquals(listOf("Food", "Groceries", "Weekly"), tree.pathTo(8).map { it.name })
    }

    @Test
    fun `path of a root is itself`() {
        assertEquals(listOf("Food"), tree.pathTo(2).map { it.name })
    }

    @Test
    fun `path of Uncategorized`() {
        assertEquals(listOf("Uncategorized"), tree.pathTo(Category.UNCATEGORIZED_ID).map { it.name })
    }

    @Test
    fun `unknown id gives an empty path`() {
        assertTrue(tree.pathTo(9999).isEmpty())
    }
}
