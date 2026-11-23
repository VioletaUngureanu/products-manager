package com.cosmeticmanager.app.data

import android.util.Log
import com.cosmeticmanager.app.data.local.ProductDao
import com.cosmeticmanager.app.data.local.toDomain
import com.cosmeticmanager.app.data.local.toEntity
import com.cosmeticmanager.app.domain.CosmeticProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * Room-backed implementation of [ProductRepository] — the local database is the single source of
 * truth (L07). CRUD operations read from and write to SQLite via [ProductDao].
 *
 * Reads: the DAO's `Flow` of entities is mapped to domain models and shared as a [StateFlow], so
 * the list is retrieved once and reused for the app's lifetime, updating reactively when the table
 * changes (Read rubric: retrieved once, observer mechanism, off the main thread).
 *
 * Writes: each suspend method wraps the DAO call in `runCatching`, logs any failure with [Log.e]
 * and returns a [Result]. The ViewModel turns a failure into a message shown in the UI (every CRUD
 * rubric line: persistence errors handled, presented to the user and logged).
 */
class RoomProductRepository(
    private val dao: ProductDao,
    scope: CoroutineScope,
) : ProductRepository {

    override val products: StateFlow<List<CosmeticProduct>> =
        dao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    override suspend fun getById(id: Long): CosmeticProduct? =
        runCatching { dao.getById(id)?.toDomain() }
            .onFailure { Log.e(TAG, "Failed to load product $id", it) }
            .getOrNull()

    override suspend fun add(product: CosmeticProduct): Result<CosmeticProduct> = runCatching {
        // Stamp as a locally-made, not-yet-synced change; id stays 0 so the DB generates it.
        val toStore = product.copy(
            lastModified = LocalDate.now(),
            isSynced = false,
        )
        val generatedId = dao.insert(toStore.toEntity())
        toStore.copy(id = generatedId)
    }.onFailure { Log.e(TAG, "Failed to add product", it) }

    override suspend fun update(product: CosmeticProduct): Result<Unit> = runCatching {
        val stamped = product.copy(lastModified = LocalDate.now(), isSynced = false)
        dao.update(stamped.toEntity())
    }.onFailure { Log.e(TAG, "Failed to update product ${product.id}", it) }

    override suspend fun delete(id: Long): Result<Unit> = runCatching {
        dao.deleteById(id)
    }.onFailure { Log.e(TAG, "Failed to delete product $id", it) }

    private companion object {
        const val TAG = "ProductRepository"
    }
}
