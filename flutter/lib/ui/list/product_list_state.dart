import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/products_repository.dart';
import '../../domain/category.dart';
import '../../domain/cosmetic_product.dart';
import '../../domain/product_filter.dart';

/// Everything the list screen needs, bundled in one immutable object.
///
/// This is the lectures' "UiState pattern": a single immutable value that
/// describes the whole screen (one observable, no scattered boolean flags,
/// trivial to test). It mirrors the professor's `SpotListUiState`.
///
/// Note what is and is NOT stored here:
///  * The raw product list lives in the data layer ([productsProvider]), the
///    single source of truth. We do NOT copy it in here.
///  * Only the *view inputs* (search text, which read-view tab, which category)
///    live here. These are "session state" that must survive the screen being
///    rebuilt, so they belong in the notifier, not in a widget variable — that
///    is the fix for the lectures' "phantom" stale-state bug.
class ProductListUiState {
  const ProductListUiState({
    this.searchQuery = '',
    this.filter = ProductFilter.all,
    this.categoryFilter,
  });

  /// The current text typed in the search box.
  final String searchQuery;

  /// Which of the four read-views (All / Unopened / Opened / About to expire).
  final ProductFilter filter;

  /// The selected category chip, or null for "all categories".
  final CosmeticCategory? categoryFilter;

  /// Returns a copy with some fields replaced. `clearCategory: true` lets a
  /// caller explicitly set the category back to null (see [copyWith] note on
  /// the product model for why a sentinel flag is needed for nullable fields).
  ProductListUiState copyWith({
    String? searchQuery,
    ProductFilter? filter,
    CosmeticCategory? categoryFilter,
    bool clearCategory = false,
  }) {
    return ProductListUiState(
      searchQuery: searchQuery ?? this.searchQuery,
      filter: filter ?? this.filter,
      categoryFilter:
          clearCategory ? null : (categoryFilter ?? this.categoryFilter),
    );
  }
}

/// Holds the list screen's view inputs and reacts to user events.
///
/// It extends `Notifier<ProductListUiState>` so the screen can observe the
/// current filters. The actual products come from [productsProvider]; this
/// notifier only tracks how to filter them.
class ProductListNotifier extends Notifier<ProductListUiState> {
  @override
  ProductListUiState build() => const ProductListUiState();

  // --- Events coming up from the UI ---

  void setSearchQuery(String query) =>
      state = state.copyWith(searchQuery: query);

  void setFilter(ProductFilter filter) => state = state.copyWith(filter: filter);

  /// Selecting a category sets it; selecting the already-selected one clears it
  /// (a toggle), which is why we need the `clearCategory` flag.
  void setCategoryFilter(CosmeticCategory? category) {
    if (category == null || category == state.categoryFilter) {
      state = state.copyWith(clearCategory: true);
    } else {
      state = state.copyWith(categoryFilter: category);
    }
  }
}

/// Provider for the list-screen view inputs.
final productListProvider =
    NotifierProvider<ProductListNotifier, ProductListUiState>(
      ProductListNotifier.new,
    );

/// A derived provider that produces the final, already-filtered list the screen
/// renders.
///
/// `ref.watch` here subscribes this provider to BOTH the raw product list and
/// the view inputs: whenever either changes, this recomputes. The screen then
/// watches only this one provider and simply draws the result — it never
/// filters by itself (the filtering lives in the pure `filterProducts`).
final visibleProductsProvider = Provider<List<CosmeticProduct>>((ref) {
  final products = ref.watch(productsProvider);
  final listState = ref.watch(productListProvider);
  return filterProducts(
    products,
    query: listState.searchQuery,
    filter: listState.filter,
    category: listState.categoryFilter,
  );
});

/// The total number of products (before filtering). Used to tell apart "you
/// have no products at all" from "your filter hid them all".
final productCountProvider = Provider<int>((ref) {
  return ref.watch(productsProvider).length;
});
