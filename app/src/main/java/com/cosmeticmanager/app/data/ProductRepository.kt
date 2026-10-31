package com.cosmeticmanager.app.data

import com.cosmeticmanager.app.domain.CosmeticProduct
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for cosmetic products (L03 Repository pattern).
 *
 * The UI never holds its own copy of the product list — it observes [products] and sends events
 * (add/update/delete) back here. This milestone is UI-only, so the backing store is in memory;
 * swapping in Room + a remote data source later means re-implementing this interface, nothing in
 * the UI layer changes.
 */
interface ProductRepository {

    /** The current list of products as an observable stream with an always-available value. */
    val products: StateFlow<List<CosmeticProduct>>

    /** Look up a single product by id, or null if it no longer exists. */
    fun getById(id: String): CosmeticProduct?

    /**
     * Add a brand-new product. The repository assigns the id and stamps it unsynced.
     * @return the stored product (with its generated id)
     */
    fun add(product: CosmeticProduct): CosmeticProduct

    /** Replace an existing product (matched by id) with an updated copy. */
    fun update(product: CosmeticProduct)

    /** Remove the product with the given id. */
    fun delete(id: String)
}
