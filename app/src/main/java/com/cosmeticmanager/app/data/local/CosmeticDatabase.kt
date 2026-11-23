package com.cosmeticmanager.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cosmeticmanager.app.domain.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * The Room database (L07 `@Database`). Holds the single products table and hands out the DAO.
 *
 * `version = 1` is the current schema. A future schema change must bump this and add a `Migration`
 * (see [build] below) — we deliberately do NOT call `fallbackToDestructiveMigration()`, because
 * that wipes all user data on upgrade (L07 warning).
 */
@Database(
    entities = [ProductEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class CosmeticDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        private const val DATABASE_NAME = "cosmetic.db"

        /**
         * Build the database. The [RoomDatabase.Callback] seeds a few demo products the first time
         * the database file is created (onCreate runs once, ever) so the list isn't empty on first
         * launch. The seed uses `upsertAll` inside a coroutine — never delete-then-insert.
         *
         * @param scope an application-scoped coroutine scope used to run the one-off seed insert.
         */
        fun build(context: Context, scope: CoroutineScope): CosmeticDatabase {
            lateinit var database: CosmeticDatabase
            database = Room.databaseBuilder(
                context.applicationContext,
                CosmeticDatabase::class.java,
                DATABASE_NAME,
            )
                // When we add schema version 2 later, replace this with .addMigrations(...).
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            database.productDao().upsertAll(seedProducts())
                        }
                    }
                })
                .build()
            return database
        }

        /** The demo products shown on a fresh install (ids left at 0 so the DB assigns them). */
        private fun seedProducts(): List<ProductEntity> {
            val today = LocalDate.now()
            return listOf(
                ProductEntity(
                    name = "Sheabutter Cream",
                    description = "Hydrating body cream for very dry skin.",
                    category = Category.BODY,
                    boughtDate = today.minusMonths(8),
                    openedDate = today.minusMonths(6),
                    shelfLifeMonths = 6,
                    ingredients = "Shea butter, glycerin, vitamin E",
                    lastModified = today,
                    isSynced = true,
                ),
                ProductEntity(
                    name = "Vitamin C Serum",
                    description = "Brightening serum for a glowing complexion.",
                    category = Category.SKINCARE,
                    boughtDate = today.minusMonths(2),
                    openedDate = today.minusMonths(1),
                    shelfLifeMonths = 2,
                    ingredients = "Ascorbic acid, ferulic acid, vitamin E",
                    lastModified = today,
                    isSynced = true,
                ),
                ProductEntity(
                    name = "Shampoo For Fine Hair",
                    description = "Strengthening shampoo that adds volume.",
                    category = Category.HAIR,
                    boughtDate = today.minusMonths(1),
                    openedDate = today.minusDays(14),
                    shelfLifeMonths = 12,
                    ingredients = "Water, keratin, panthenol",
                    lastModified = today,
                    isSynced = true,
                ),
                ProductEntity(
                    name = "Face Cleanser",
                    description = "Cleans skin and removes makeup gently.",
                    category = Category.FACE,
                    boughtDate = today.minusDays(3),
                    openedDate = null,
                    shelfLifeMonths = 24,
                    ingredients = "Water, glycerin, mild surfactants",
                    lastModified = today,
                    isSynced = true,
                ),
            )
        }
    }
}
