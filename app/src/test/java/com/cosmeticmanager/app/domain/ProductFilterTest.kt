package com.cosmeticmanager.app.domain

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the pure [filterProducts] helper (L03 "push logic into pure functions").
 *
 * These run in milliseconds with no device — the whole point of keeping filtering logic out of the
 * composable and in a plain function.
 */
class ProductFilterTest {

    private val today: LocalDate = LocalDate.now()

    private fun product(
        id: Long,
        name: String,
        category: Category,
        openedDate: LocalDate? = null,
        shelfLifeMonths: Int = 12,
        description: String = "",
    ) = CosmeticProduct(
        id = id,
        name = name,
        description = description,
        category = category,
        openedDate = openedDate,
        shelfLifeMonths = shelfLifeMonths,
    )

    private val sample = listOf(
        // Unopened
        product(1, "Face Cleanser", Category.FACE),
        // Opened, lots of life left
        product(2, "Shampoo", Category.HAIR, openedDate = today.minusDays(10), shelfLifeMonths = 12),
        // Opened, about to expire (opened 11 months ago, 12-month shelf life -> ~30 days left)
        product(3, "Vitamin C Serum", Category.SKINCARE, openedDate = today.minusMonths(11), shelfLifeMonths = 12),
        // Opened and already expired
        product(4, "Old Cream", Category.BODY, openedDate = today.minusMonths(13), shelfLifeMonths = 6),
    )

    @Test
    fun `ALL with blank query returns every product`() {
        val result = sample.filterProducts(query = "", filter = ProductFilter.ALL, category = null)
        assertEquals(4, result.size)
    }

    @Test
    fun `UNOPENED returns only products without an opened date`() {
        val result = sample.filterProducts("", ProductFilter.UNOPENED, null)
        assertEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun `OPENED returns only products with an opened date`() {
        val result = sample.filterProducts("", ProductFilter.OPENED, null)
        assertEquals(listOf(2L, 3L, 4L), result.map { it.id })
    }

    @Test
    fun `ABOUT_TO_EXPIRE returns opened products near or past expiry`() {
        val result = sample.filterProducts("", ProductFilter.ABOUT_TO_EXPIRE, null)
        // #3 (about to expire) and #4 (expired) qualify; #2 still has plenty of life.
        assertEquals(setOf(3L, 4L), result.map { it.id }.toSet())
    }

    @Test
    fun `query is trimmed and case-insensitive and matches name`() {
        val result = sample.filterProducts("  shampoo  ", ProductFilter.ALL, null)
        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test
    fun `query also matches description`() {
        val list = listOf(
            product(99, "Mystery Potion", Category.OTHER, description = "contains retinol"),
        )
        val result = list.filterProducts("retinol", ProductFilter.ALL, null)
        assertTrue(result.any { it.id == 99L })
    }

    @Test
    fun `category narrows the result`() {
        val result = sample.filterProducts("", ProductFilter.ALL, Category.HAIR)
        assertEquals(listOf(2L), result.map { it.id })
    }

    @Test
    fun `filter and category combine`() {
        val result = sample.filterProducts("", ProductFilter.OPENED, Category.SKINCARE)
        assertEquals(listOf(3L), result.map { it.id })
    }
}
