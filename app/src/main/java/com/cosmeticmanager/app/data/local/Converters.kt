package com.cosmeticmanager.app.data.local

import androidx.room.TypeConverter
import com.cosmeticmanager.app.domain.Category
import java.time.LocalDate

/**
 * Tells Room how to store the types that SQLite does not understand natively (L07 `@TypeConverter`).
 *
 * - [LocalDate] <-> epoch day (a Long count of days since 1970-01-01). Compact, sortable, and
 *   timezone-free, which suits a plain calendar date.
 * - [Category] <-> its enum constant name (a String). Storing the name, not the ordinal, means
 *   reordering the enum later never corrupts existing rows.
 *
 * Each converter is null-safe so optional columns (e.g. boughtDate) round-trip correctly.
 */
class Converters {

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? =
        epochDay?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun categoryToName(category: Category): String = category.name

    @TypeConverter
    fun nameToCategory(name: String): Category = Category.valueOf(name)
}
