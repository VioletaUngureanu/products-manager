package com.cosmeticmanager.app.data

import android.content.Context
import com.cosmeticmanager.app.data.local.CosmeticDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * A tiny manual dependency container.
 *
 * The lectures use Hilt for DI, but that's introduced later in the course; for this milestone a
 * single shared repository instance is all we need. [initialize] is called once from the
 * [com.cosmeticmanager.app.CosmeticApplication] so the repository can be built with the app
 * [Context] (needed to open the Room database).
 *
 * Exposing one shared repository guarantees list/create/edit/detail all observe the SAME source of
 * truth — the local database.
 */
object ServiceLocator {

    // Application-scoped: lives as long as the process. Used by Room to share the products Flow
    // and to run the one-off first-launch seed. SupervisorJob so one failed child never cancels it.
    private val appScope = CoroutineScope(SupervisorJob())

    @Volatile
    private var repository: ProductRepository? = null

    /** Build the repository once, from the Application's onCreate. Safe to call more than once. */
    fun initialize(context: Context) {
        if (repository != null) return
        synchronized(this) {
            if (repository == null) {
                val database = CosmeticDatabase.build(context.applicationContext, appScope)
                repository = RoomProductRepository(database.productDao(), appScope)
            }
        }
    }

    /** The shared repository. [initialize] must have been called first (it is, from the App). */
    val productRepository: ProductRepository
        get() = repository
            ?: error("ServiceLocator.initialize(context) must be called before use")
}
