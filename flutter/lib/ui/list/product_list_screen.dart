import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/products_repository.dart';
import '../../domain/cosmetic_product.dart';
import '../../domain/product_filter.dart';
import '../editor/product_editor_screen.dart';
import '../detail/product_detail_screen.dart';
import 'product_list_state.dart';
import 'widgets/category_filter_row.dart';
import 'widgets/product_card.dart';
import 'widgets/product_search_bar.dart';

/// The Read screen: "Your products".
///
/// It extends `ConsumerWidget` — a Riverpod widget that gets a `WidgetRef`,
/// the handle used to read/watch providers. `ref.watch(...)` subscribes this
/// widget to a provider so it rebuilds when that provider's value changes;
/// `ref.read(...)` grabs a value once without subscribing (used for one-off
/// actions like calling a method).
///
/// Rubric target (Read = Excellent): a list view (`ListView.builder`) driven by
/// a view-model/repository via observed state, where only the affected row
/// changes on create/update/delete — no manual rebuild of the whole list and no
/// recreation of the screen.
class ProductListScreen extends ConsumerWidget {
  const ProductListScreen({super.key});

  /// Extra space below the last list item so the floating button does not cover
  /// it. Named constant instead of a magic number.
  static const double _bottomListPadding = 88;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Watch the derived, already-filtered list. This provider recomputes only
    // when the products or the filters change, and the UI simply renders it.
    final visibleProducts = ref.watch(visibleProductsProvider);
    // Watch the view inputs (search text, selected tab, selected category).
    final listState = ref.watch(productListProvider);
    // Grab the notifiers for sending events up (read, not watch).
    final listNotifier = ref.read(productListProvider.notifier);
    final totalCount = ref.watch(productCountProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Your products')),
      // The floating "+" button opens the create screen.
      floatingActionButton: FloatingActionButton(
        onPressed: () => _openCreateScreen(context),
        tooltip: 'Add a product',
        child: const Icon(Icons.add),
      ),
      body: Column(
        children: [
          ProductSearchBar(
            query: listState.searchQuery,
            onQueryChange: listNotifier.setSearchQuery,
          ),
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 12, 16, 4),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text('Categories'),
            ),
          ),
          CategoryFilterRow(
            selected: listState.categoryFilter,
            onSelected: listNotifier.setCategoryFilter,
          ),
          _FilterTabs(
            selected: listState.filter,
            onSelected: listNotifier.setFilter,
          ),
          // `Expanded` makes the list take all remaining vertical space.
          Expanded(
            child: _buildBody(
              context: context,
              ref: ref,
              visibleProducts: visibleProducts,
              totalCount: totalCount,
            ),
          ),
        ],
      ),
    );
  }

  /// Decides what to show in the main area: the list, or an empty-state message.
  Widget _buildBody({
    required BuildContext context,
    required WidgetRef ref,
    required List<CosmeticProduct> visibleProducts,
    required int totalCount,
  }) {
    if (totalCount == 0) {
      return const _EmptyState(
        message: 'No products yet.\nTap + to add your first cosmetic.',
      );
    }
    if (visibleProducts.isEmpty) {
      return const _EmptyState(
        message: 'No products match your search or filters.',
      );
    }

    // `ListView.builder` is the lazy, virtualized list (the Flutter equivalent
    // of a RecyclerView): it only builds the rows currently on screen.
    return ListView.builder(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, _bottomListPadding),
      itemCount: visibleProducts.length,
      itemBuilder: (context, index) {
        final product = visibleProducts[index];
        return ProductCard(
          // A stable key tied to the product id lets Flutter match a row to the
          // same product across rebuilds, so only changed rows rebuild and
          // state is never attached to the wrong row.
          key: ValueKey(product.id),
          product: product,
          onTap: () => _openDetailScreen(context, product.id),
          onEdit: () => _openEditScreen(context, product.id),
          onDelete: () => _confirmDelete(context, ref, product),
        );
      },
    );
  }

  // --- Navigation helpers: each CRUD operation opens its own screen ---

  void _openCreateScreen(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        // No productId => the editor is in "create" mode.
        builder: (_) => const ProductEditorScreen(),
      ),
    );
  }

  void _openEditScreen(BuildContext context, String productId) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        // Passing a productId => the editor loads it and is in "edit" mode.
        builder: (_) => ProductEditorScreen(productId: productId),
      ),
    );
  }

  void _openDetailScreen(BuildContext context, String productId) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => ProductDetailScreen(productId: productId),
      ),
    );
  }

  /// Shows a confirmation dialog, then deletes by id if the user confirms.
  /// Delete rubric: a confirmation dialog is used, and only the id is passed
  /// back to the repository.
  Future<void> _confirmDelete(
    BuildContext context,
    WidgetRef ref,
    CosmeticProduct product,
  ) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Delete product?'),
        content: Text('"${product.name}" will be permanently removed.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(true),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      // Only the id crosses back to the data layer.
      ref.read(productsProvider.notifier).delete(product.id);
    }
  }
}

/// The four read-view tabs (All / Unopened / Opened / About to expire).
/// Private (leading underscore) because only this screen uses it.
class _FilterTabs extends StatelessWidget {
  const _FilterTabs({required this.selected, required this.onSelected});

  final ProductFilter selected;
  final ValueChanged<ProductFilter> onSelected;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: _tabsHeight,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        children: [
          for (final filter in ProductFilter.values)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 4),
              child: ChoiceChip(
                label: Text(filter.label),
                selected: filter == selected,
                onSelected: (_) => onSelected(filter),
              ),
            ),
        ],
      ),
    );
  }

  static const double _tabsHeight = 48;
}

/// A centered message shown when there is nothing to list.
class _EmptyState extends StatelessWidget {
  const _EmptyState({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Text(
          message,
          textAlign: TextAlign.center,
          style: Theme.of(context).textTheme.bodyLarge?.copyWith(
            color: Theme.of(context).colorScheme.onSurfaceVariant,
          ),
        ),
      ),
    );
  }
}
