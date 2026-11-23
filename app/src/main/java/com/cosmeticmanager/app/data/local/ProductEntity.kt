package com.cosmeticmanager.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cosmeticmanager.app.domain.Category
import java.time.LocalDate

/**
 * The Room table row for one cosmetic product (L07 `@Entity`).
 *
 * This is the *storage* shape, deliberately separate from the domain [com.cosmeticmanager.app.domain.CosmeticProduct]:
 * the UI and ViewModels keep depending on the domain model, and [ProductMappers] converts between
 * the two. Keeping them apart means a future schema change (a new column, a renamed field) touches
 * only this file and the mapper, never the UI.
 *
 * Types stored: Room/SQLite handles primitives directly. [LocalDate] and [Category] are stored via
 * the `@TypeConverters` in [Converters] (dates as epoch-day Long, category as its enum name).
 */
@Entity(tableName = "products")
data class ProductEntity(
    // autoGenerate = true: the database assigns the id. We insert with id = 0 and SQLite fills in
    // the real primary key. This is the rubric's "id is managed by the DB" requirement.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,
    val description: String,
    val category: Category,
    val boughtDate: LocalDate?,
    val openedDate: LocalDate?,
    val shelfLifeMonths: Int,
    val ingredients: String,

    val lastModified: LocalDate,
    // Marks a row whose change the server has not confirmed yet. Unused in this local-only
    // milestone, but persisted now so the next assignment (server sync) needs no schema change
    // (L07: "add a pendingSync column now, it makes Assignment 5 easier").
    val isSynced: Boolean,
)
