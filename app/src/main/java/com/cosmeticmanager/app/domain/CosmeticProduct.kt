package com.cosmeticmanager.app.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * A single cosmetic product — the one and only domain entity of the app.
 *
 * Modelled as an immutable Kotlin `data class` (L02): we never mutate an instance, we create a
 * modified copy via [copy]. This keeps a single source of truth and makes the UDF loop predictable
 * (L03): the repository holds the list, the UI only ever reads copies.
 *
 * Fields follow the README's domain spec. "shelf life" is stored as a number of months, as agreed.
 */
data class CosmeticProduct(
    // --- internal fields ---
    val id: String,
    val lastModified: LocalDate = LocalDate.now(),
    // "sent to the server" — kept in the model so the list can show a "waiting to sync" icon.
    // In this UI-only milestone nothing actually syncs; every locally-made change starts unsynced.
    val isSynced: Boolean = false,

    // --- external fields ---
    val name: String,                       // required
    val description: String = "",           // optional
    val category: Category,                 // required
    val boughtDate: LocalDate? = null,      // optional
    val openedDate: LocalDate? = null,      // optional, and write-once (see Update rules)
    val shelfLifeMonths: Int,               // required (months the product lasts once opened)
    val ingredients: String = "",           // optional
) {
    /** A product is "opened" once an opened date has been set. */
    val isOpened: Boolean get() = openedDate != null

    /**
     * The date an opened product stops being safe to use: openedDate + shelfLifeMonths.
     * Null while the product is still unopened (shelf life only starts counting once opened).
     */
    val expiryDate: LocalDate?
        get() = openedDate?.plusMonths(shelfLifeMonths.toLong())

    /** Whole days until expiry (negative if already expired). Null when unopened. */
    val daysUntilExpiry: Long?
        get() = expiryDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }

    /** True once an opened product is past its expiry date. */
    val isExpired: Boolean
        get() = daysUntilExpiry?.let { it < 0 } ?: false

    /**
     * "About to expire": the product was opened and its shelf life is almost over.
     * We treat "almost over" as within [ABOUT_TO_EXPIRE_THRESHOLD_DAYS] days (or already expired).
     */
    val isAboutToExpire: Boolean
        get() = daysUntilExpiry?.let { it <= ABOUT_TO_EXPIRE_THRESHOLD_DAYS } ?: false

    companion object {
        const val ABOUT_TO_EXPIRE_THRESHOLD_DAYS: Long = 30
    }
}
