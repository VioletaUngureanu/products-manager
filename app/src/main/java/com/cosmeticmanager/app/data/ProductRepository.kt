package com.cosmeticmanager.app.data

import com.cosmeticmanager.app.domain.CosmeticProduct
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for cosmetic products (L07 Repository pattern).
 *
 * The UI never touches the DAO directly — it observes [products] and calls the suspend write
 * methods below, which run off the main thread. This is the one seam between the UI layer and the
 * local database; swapping the database for a server later means re-implementing this interface
 * only, with no change to the UI.
 *
 * Write methods return [Result] so the caller (the ViewModel) can show and log a persistence error
 * instead of crashing — the rubric requires errors handled, presented to the user and logged.
 */
interface ProductRepository {

    /**
     * The current list of products as an observable stream with an always-available value.
     * Backed by a Room `Flow`, so a create/update/delete is reflected here automatically.
     */
    val products: StateFlow<List<CosmeticProduct>>

    /** Look up a single product by id, or null if it no longer exists. Runs off the main thread. */
    suspend fun getById(id: Long): CosmeticProduct?

    /**
     * Insert a brand-new product. The database assigns the id.
     * @return [Result] with the stored product (carrying its generated id) on success.
     */
    suspend fun add(product: CosmeticProduct): Result<CosmeticProduct>

    /** Update an existing product in place (same row, same id — never delete+insert). */
    suspend fun update(product: CosmeticProduct): Result<Unit>

    /** Remove the product with the given id. */
    suspend fun delete(id: Long): Result<Unit>
}
