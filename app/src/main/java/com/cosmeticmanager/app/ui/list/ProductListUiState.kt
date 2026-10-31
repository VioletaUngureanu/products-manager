package com.cosmeticmanager.app.ui.list

import com.cosmeticmanager.app.domain.Category
import com.cosmeticmanager.app.domain.CosmeticProduct
import com.cosmeticmanager.app.domain.ProductFilter

/**
 * Everything the product-list screen needs, in one immutable object (L03 "UiState pattern").
 *
 * [visibleProducts] is the already-filtered list the UI renders — the composable never filters.
 */
data class ProductListUiState(
    val visibleProducts: List<CosmeticProduct> = emptyList(),
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val filter: ProductFilter = ProductFilter.ALL,
    val categoryFilter: Category? = null,
) {
    /** True when there are products overall but the current search/filter hides them all. */
    val isEmptyBecauseOfFilters: Boolean
        get() = visibleProducts.isEmpty() && totalCount > 0

    /** True when there are no products at all. */
    val isCompletelyEmpty: Boolean
        get() = totalCount == 0
}
