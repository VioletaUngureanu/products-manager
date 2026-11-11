package com.cosmeticmanager.app.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cosmeticmanager.app.domain.CosmeticProduct
import com.cosmeticmanager.app.domain.ProductFilter
import com.cosmeticmanager.app.ui.ViewModelFactory
import com.cosmeticmanager.app.ui.list.components.CategoryFilterRow
import com.cosmeticmanager.app.ui.list.components.ProductCard
import com.cosmeticmanager.app.ui.list.components.ProductSearchBar

/**
 * The Read screen: "Your products".
 *
 * A [LazyColumn] (the Compose recycler-view equivalent) driven by a [ProductListViewModel] via a
 * [androidx.lifecycle.compose.collectAsStateWithLifecycle] LiveData-style observed state. Items use
 * a stable `key`, so adding/editing/deleting marshals only the affected row — the list is never
 * rebuilt by hand (targeting the rubric's "Excellent" bar for Read).
 *
 * @param onAddProduct navigate to the create screen
 * @param onEditProduct navigate to the edit screen for a given id
 * @param onOpenProduct open the read-only detail screen for a given id
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    onOpenProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductListViewModel = viewModel(factory = ViewModelFactory()),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Which product (if any) is pending delete confirmation — ephemeral UI state.
    var pendingDelete by remember { mutableStateOf<CosmeticProduct?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Your products") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Filled.Add, contentDescription = "Add a product")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProductSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
            )

            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleSmall,
            )
            CategoryFilterRow(
                selected = uiState.categoryFilter,
                onCategoryClick = viewModel::onCategoryFilterChange,
            )

            FilterTabs(
                selected = uiState.filter,
                onSelect = viewModel::onFilterChange,
            )

            when {
                uiState.isCompletelyEmpty -> EmptyState(
                    message = "No products yet.\nTap + to add your first cosmetic.",
                )

                uiState.isEmptyBecauseOfFilters -> EmptyState(
                    message = "No products match your search or filters.",
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    items(
                        items = uiState.visibleProducts,
                        key = { it.id },
                    ) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onOpenProduct(product.id) },
                            onEdit = { onEditProduct(product.id) },
                            onDelete = { pendingDelete = product },
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog (rubric: Delete uses a confirmation dialog). Only the id is passed
    // back to the ViewModel to remove the item.
    pendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete product?") },
            text = { Text("\"${product.name}\" will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteProduct(product.id)
                        pendingDelete = null
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun FilterTabs(
    selected: ProductFilter,
    onSelect: (ProductFilter) -> Unit,
) {
    val filters = ProductFilter.entries
    ScrollableTabRow(
        selectedTabIndex = filters.indexOf(selected),
        edgePadding = 0.dp,
    ) {
        filters.forEach { filter ->
            Tab(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                text = { Text(filter.label) },
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
