package com.cosmeticmanager.app.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmeticmanager.app.data.ProductRepository
import com.cosmeticmanager.app.domain.Category
import com.cosmeticmanager.app.domain.ProductFilter
import com.cosmeticmanager.app.domain.filterProducts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State holder for the product-list screen (L03 ViewModel + StateFlow).
 *
 * The search query, active filter and category filter are session state and live HERE, not in a
 * local composable variable — so they survive rotation (the "phantom rating" / stale-search bug).
 * The product list itself is owned by the repository (single source of truth); this ViewModel only
 * combines it with the current filters into a derived, immutable [ProductListUiState].
 *
 * Because the list is a Flow derived from the repository's StateFlow, a create/update/delete on
 * another screen flows straight back into this list with no manual refresh — only the affected
 * item changes; the list/adapter is never rebuilt by hand.
 */
class ProductListViewModel(
    private val repository: ProductRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filter = MutableStateFlow(ProductFilter.ALL)
    private val categoryFilter = MutableStateFlow<Category?>(null)

    // Holds a message when a database operation fails (e.g. delete). Folded into uiState below so
    // the screen can show it; cleared via onErrorShown.
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProductListUiState> =
        combine(
            repository.products,
            searchQuery,
            filter,
            categoryFilter,
            errorMessage,
        ) { allProducts, query, activeFilter, category, error ->
            ProductListUiState(
                visibleProducts = allProducts.filterProducts(query, activeFilter, category),
                totalCount = allProducts.size,
                searchQuery = query,
                filter = activeFilter,
                categoryFilter = category,
                errorMessage = error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProductListUiState(),
        )

    // --- events flowing UP from the UI ---

    fun onSearchQueryChange(query: String) = searchQuery.update { query }

    fun onFilterChange(newFilter: ProductFilter) = filter.update { newFilter }

    fun onCategoryFilterChange(category: Category?) = categoryFilter.update {
        // Tapping the already-selected category clears it (toggle behaviour).
        if (it == category) null else category
    }

    /**
     * Delete is handled here so the list screen only passes back the id (rubric: pass the id).
     * Runs in viewModelScope (off the main thread, via the suspend repository call). On a database
     * failure the repository has already logged it; we surface a message for the user. On success
     * the repository's Flow emits and the row disappears from the list automatically.
     */
    fun onDeleteProduct(id: Long) {
        viewModelScope.launch {
            repository.delete(id).onFailure {
                errorMessage.value = "Couldn't delete the product. Please try again."
            }
        }
    }

    /** Called by the screen after it has shown the error, so the same message isn't shown again. */
    fun onErrorShown() {
        errorMessage.value = null
    }
}
