package com.cosmeticmanager.app.data

import com.cosmeticmanager.app.domain.Category
import com.cosmeticmanager.app.domain.CosmeticProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.UUID

/**
 * In-memory implementation of [ProductRepository], seeded with a handful of demo products so the
 * list isn't empty on first launch. Data lives only for the lifetime of the process (no disk, no
 * network yet) — exactly the "fake/in-memory data" the assignment asks to demo CRUD against.
 *
 * The writable [MutableStateFlow] is private; callers only see the read-only [StateFlow]
 * (L03: never expose a mutable state surface).
 */
class InMemoryProductRepository : ProductRepository {

    private val _products = MutableStateFlow(seedProducts())
    override val products: StateFlow<List<CosmeticProduct>> = _products.asStateFlow()

    override fun getById(id: String): CosmeticProduct? =
        _products.value.firstOrNull { it.id == id }

    override fun add(product: CosmeticProduct): CosmeticProduct {
        val stored = product.copy(
            id = UUID.randomUUID().toString(),
            lastModified = LocalDate.now(),
            isSynced = false,
        )
        _products.update { current -> current + stored }
        return stored
    }

    override fun update(product: CosmeticProduct) {
        val stamped = product.copy(lastModified = LocalDate.now(), isSynced = false)
        _products.update { current ->
            current.map { if (it.id == stamped.id) stamped else it }
        }
    }

    override fun delete(id: String) {
        _products.update { current -> current.filterNot { it.id == id } }
    }

    private companion object {
        /** A small set of demo products that mirrors the mockup's example list. */
        fun seedProducts(): List<CosmeticProduct> {
            val today = LocalDate.now()
            return listOf(
                CosmeticProduct(
                    id = UUID.randomUUID().toString(),
                    name = "Sheabutter Cream",
                    description = "Hydrating body cream for very dry skin.",
                    category = Category.BODY,
                    boughtDate = today.minusMonths(8),
                    openedDate = today.minusMonths(6),
                    shelfLifeMonths = 6,
                    ingredients = "Shea butter, glycerin, vitamin E",
                    isSynced = true,
                ),
                CosmeticProduct(
                    id = UUID.randomUUID().toString(),
                    name = "Vitamin C Serum",
                    description = "Brightening serum for a glowing complexion.",
                    category = Category.SKINCARE,
                    boughtDate = today.minusMonths(2),
                    openedDate = today.minusMonths(1),
                    shelfLifeMonths = 2,
                    ingredients = "Ascorbic acid, ferulic acid, vitamin E",
                    isSynced = true,
                ),
                CosmeticProduct(
                    id = UUID.randomUUID().toString(),
                    name = "Shampoo For Fine Hair",
                    description = "Strengthening shampoo that adds volume.",
                    category = Category.HAIR,
                    boughtDate = today.minusMonths(1),
                    openedDate = today.minusDays(14),
                    shelfLifeMonths = 12,
                    ingredients = "Water, keratin, panthenol",
                    isSynced = true,
                ),
                CosmeticProduct(
                    id = UUID.randomUUID().toString(),
                    name = "Face Cleanser",
                    description = "Cleans skin and removes makeup gently.",
                    category = Category.FACE,
                    boughtDate = today.minusDays(3),
                    openedDate = null,
                    shelfLifeMonths = 24,
                    ingredients = "Water, glycerin, mild surfactants",
                    isSynced = true,
                ),
            )
        }
    }
}
