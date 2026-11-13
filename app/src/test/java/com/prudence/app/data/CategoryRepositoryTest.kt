package com.prudence.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.prudence.app.domain.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class CategoryRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var categories: CategoryRepository
    private lateinit var expenses: ExpenseRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(CategorySeeds.callback)
            .build()
        categories = CategoryRepository(db, db.categoryDao())
        expenses = ExpenseRepository(db.expenseDao())
    }

    @After
    fun tearDown() = db.close()

    private suspend fun all(): List<Category> = categories.observeCategories().first()
    private suspend fun byId(id: Long): Category? = all().firstOrNull { it.id == id }

    @Test
    fun `fresh database seeds Uncategorized and five default roots`() = runTest {
        val names = all().map { it.name }
        assertTrue(names.containsAll(listOf("Uncategorized", "Food", "Transport", "Housing", "Health", "Entertainment")))
        assertTrue(all().all { it.parentId == null })
    }

    @Test
    fun `deleting a category reassigns its expenses and reparents its children to its parent`() = runTest {
        val groceries = categories.addCategory("Groceries", parentId = 2)!! // under Food
        val weekly = categories.addCategory("Weekly", parentId = groceries)!!
        expenses.addExpense(amountMinor = 4210, date = LocalDate.now(), categoryId = groceries)

        assertTrue(categories.deleteCategory(groceries))

        val expense = expenses.observeExpenses().first().single()
        assertEquals(2L, expense.categoryId) // reassigned to Food
        assertEquals(2L, byId(weekly)?.parentId) // Weekly reparented to Food
        assertNull(byId(groceries))
    }

    @Test
    fun `deleting a root category sinks its expenses and children to Uncategorized`() = runTest {
        val fuel = categories.addCategory("Fuel", parentId = 3)!! // under Transport
        expenses.addExpense(amountMinor = 3800, date = LocalDate.now(), categoryId = 3)

        assertTrue(categories.deleteCategory(3)) // delete Transport (root)

        val expense = expenses.observeExpenses().first().single()
        assertEquals(Category.UNCATEGORIZED_ID, expense.categoryId)
        assertEquals(Category.UNCATEGORIZED_ID, byId(fuel)?.parentId)
        assertNull(byId(3))
    }

    @Test
    fun `moving a category under a new parent keeps its subtree`() = runTest {
        val groceries = categories.addCategory("Groceries", parentId = 2)!!
        val weekly = categories.addCategory("Weekly", parentId = groceries)!!

        assertTrue(categories.moveCategory(groceries, newParentId = 3)) // under Transport

        assertEquals(3L, byId(groceries)?.parentId)
        assertEquals(groceries, byId(weekly)?.parentId) // subtree intact
    }

    @Test
    fun `moving a category under itself or its own descendant is rejected`() = runTest {
        val groceries = categories.addCategory("Groceries", parentId = 2)!!

        assertFalse(categories.moveCategory(2, newParentId = 2)) // Food under Food
        assertFalse(categories.moveCategory(2, newParentId = groceries)) // Food under its child
        assertNull(byId(2)?.parentId) // Food still a root
    }

    @Test
    fun `Uncategorized cannot be renamed, moved, or deleted`() = runTest {
        assertFalse(categories.renameCategory(Category.UNCATEGORIZED_ID, "Misc"))
        assertFalse(categories.moveCategory(Category.UNCATEGORIZED_ID, newParentId = 2))
        assertFalse(categories.deleteCategory(Category.UNCATEGORIZED_ID))

        val uncategorized = byId(Category.UNCATEGORIZED_ID)
        assertNotNull(uncategorized)
        assertEquals("Uncategorized", uncategorized?.name)
        assertNull(uncategorized?.parentId)
    }

    @Test
    fun `moving under a non-existent parent is rejected`() = runTest {
        assertFalse(categories.moveCategory(2, newParentId = 9999))
        assertNull(byId(2)?.parentId) // Food still a root
    }

    @Test
    fun `the name Uncategorized is reserved`() = runTest {
        assertNull(categories.addCategory("Uncategorized", parentId = null))
        assertNull(categories.addCategory("uncategorized", parentId = 2))
        assertFalse(categories.renameCategory(2, "UNCATEGORIZED")) // Food stays Food
        assertEquals("Food", byId(2)?.name)
    }

    @Test
    fun `add and rename a category`() = runTest {
        val id = categories.addCategory("Coffee", parentId = 2)!!
        assertEquals("Coffee", byId(id)?.name)
        assertEquals(2L, byId(id)?.parentId)

        assertTrue(categories.renameCategory(id, "Coffee & snacks"))
        assertEquals("Coffee & snacks", byId(id)?.name)
    }
}
