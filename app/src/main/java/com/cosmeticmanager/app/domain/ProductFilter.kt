package com.cosmeticmanager.app.domain

/**
 * The four "Read" views from the README: all / unopened / opened / about-to-expire.
 */
enum class ProductFilter(val label: String) {
    ALL("All"),
    UNOPENED("Unopened"),
    OPENED("Opened"),
    ABOUT_TO_EXPIRE("About to expire"),
}

/**
 * Pure, side-effect-free filtering used by the list screen's UiState.
 *
 * Kept as an extension function on `List<CosmeticProduct>` so it can be unit-tested in
 * milliseconds without a device or ViewModel (L03 "push logic into pure functions"). The list
 * screen simply calls this inside its state derivation; it never filters in the composable.
 *
 * @param query free-text search matched against name and description (trimmed, case-insensitive)
 * @param filter which of the four read views to apply
 * @param category optional category narrowing; null means "any category"
 */
fun List<CosmeticProduct>.filterProducts(
    query: String,
    filter: ProductFilter,
    category: Category?,
): List<CosmeticProduct> {
    val trimmedQuery = query.trim()
    return filter { product ->
        val matchesQuery = trimmedQuery.isEmpty() ||
            product.name.contains(trimmedQuery, ignoreCase = true) ||
            product.description.contains(trimmedQuery, ignoreCase = true)

        val matchesCategory = category == null || product.category == category

        val matchesFilter = when (filter) {
            ProductFilter.ALL -> true
            ProductFilter.UNOPENED -> !product.isOpened
            ProductFilter.OPENED -> product.isOpened
            ProductFilter.ABOUT_TO_EXPIRE -> product.isAboutToExpire
        }

        matchesQuery && matchesCategory && matchesFilter
    }
}
