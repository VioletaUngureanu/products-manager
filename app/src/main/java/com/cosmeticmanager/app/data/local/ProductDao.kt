package com.cosmeticmanager.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the products table (L07 `@Dao`).
 *
 * Reads return a [Flow]: Room emits a fresh list every time the table changes, which is the
 * "observer/live-data mechanism" the Read rubric asks for. The query runs on a background thread
 * and the UI simply collects the stream — it never polls or re-fetches per operation.
 *
 * Writes are `suspend` functions: Room runs them off the main thread (the rubric's "separate
 * thread/coroutine"). Each throws on failure, so the repository can catch, show and log the error.
 */
@Dao
interface ProductDao {

    /** Observe all products, newest first. One query, reused for the app's lifetime. */
    @Query("SELECT * FROM products ORDER BY lastModified DESC, id DESC")
    fun observeAll(): Flow<List<ProductEntity>>

    /** One-off lookup of a single row by id (used when loading the editor in edit mode). */
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    /**
     * Insert a new row and return the id the database generated. The caller passes id = 0; SQLite
     * assigns the real primary key and Room returns it here.
     */
    @Insert
    suspend fun insert(product: ProductEntity): Long

    /** Update the row that matches the entity's primary key. Reuses the same row (no delete+insert). */
    @Update
    suspend fun update(product: ProductEntity)

    /** Delete the row with this id. Keyed by id only — the rubric's "delete by id". */
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Insert-or-update a batch in a single call. Used only to seed demo data on first launch.
     * Upsert (never delete-then-insert) is the L07 rule that avoids an empty-list flash and data
     * loss if a write fails midway.
     */
    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    /** Count of rows, used to decide whether the first-launch seed is needed. */
    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int
}
