import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/products_repository.dart';
import '../../domain/cosmetic_product.dart';
import '../common/category_icon.dart';
import '../common/date_formatting.dart';
import '../editor/product_editor_screen.dart';

/// A read-only screen showing every field of a single product.
///
/// It receives only a [productId] (not the whole object) and looks the product
/// up in the single source of truth — the lectures' "pass ids, not objects"
/// rule. It offers quick Edit and Delete actions in the app bar.
class ProductDetailScreen extends ConsumerWidget {
  const ProductDetailScreen({super.key, required this.productId});

  final String productId;

  /// Size of the large category icon in the header.
  static const double _headerIconSize = 56;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Watch the product list and find this product. Watching (not reading)
    // means that if the product is edited elsewhere, this screen updates too.
    final products = ref.watch(productsProvider);
    final product = _findOrNull(products, productId);

    // The product may be null if it was just deleted while this screen was open.
    if (product == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Product')),
        body: const Center(child: Text('This product no longer exists.')),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(product.name),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit),
            tooltip: 'Edit',
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute<void>(
                builder: (_) => ProductEditorScreen(productId: product.id),
              ),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.delete),
            tooltip: 'Delete',
            onPressed: () => _confirmDelete(context, ref, product),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header: big category icon + name + opened/unopened status.
            Row(
              children: [
                CategoryIcon(category: product.category, size: _headerIconSize),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        product.name,
                        style: Theme.of(context).textTheme.headlineSmall,
                      ),
                      Text(
                        product.isOpened ? 'Opened' : 'Unopened',
                        style: Theme.of(context).textTheme.labelLarge?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const Divider(height: 32),

            // One labelled row per field.
            _DetailRow(label: 'Category', value: product.category.label),
            _DetailRow(
              label: 'Description',
              value: product.description.isEmpty ? '—' : product.description,
            ),
            _DetailRow(
              label: 'Shelf life',
              value: '${product.shelfLifeMonths} months',
            ),
            _DetailRow(
              label: 'Bought date',
              value: formatDateOrDash(product.boughtDate),
            ),
            _DetailRow(
              label: 'Opened date',
              value: formatDateOrDash(product.openedDate),
            ),
            _DetailRow(
              label: 'Expiry',
              value: formatDateOrDash(product.expiryDate),
            ),
            _DetailRow(label: 'Status', value: expiryStatusText(product)),
            _DetailRow(
              label: 'Ingredients',
              value: product.ingredients.isEmpty ? '—' : product.ingredients,
            ),
          ],
        ),
      ),
    );
  }

  /// Finds the product with [id], or returns null if not present.
  CosmeticProduct? _findOrNull(List<CosmeticProduct> products, String id) {
    for (final product in products) {
      if (product.id == id) return product;
    }
    return null;
  }

  /// Confirmation dialog + delete-by-id, then return to the list.
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
      ref.read(productsProvider.notifier).delete(product.id);
      // Close the detail screen so we land back on the list.
      if (context.mounted) Navigator.of(context).pop();
    }
  }
}

/// A single labelled read-only row on the detail screen.
class _DetailRow extends StatelessWidget {
  const _DetailRow({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: theme.textTheme.labelMedium?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          Text(value, style: theme.textTheme.bodyLarge),
        ],
      ),
    );
  }
}
