package com.cosmeticmanager.app.data

/**
 * A tiny manual dependency container.
 *
 * The lectures use Hilt for DI (L03), but that's introduced later in the course; for this UI-only
 * milestone a single shared repository instance is all we need. Exposing it here keeps the
 * ViewModels free of construction details and guarantees list/create/edit all observe the SAME
 * source of truth.
 */
object ServiceLocator {
    val productRepository: ProductRepository by lazy { InMemoryProductRepository() }
}
