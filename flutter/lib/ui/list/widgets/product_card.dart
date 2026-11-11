import 'package:flutter/material.dart';

import '../../../domain/cosmetic_product.dart';
import '../../common/category_icon.dart';
import '../../common/date_formatting.dart';

/// Identifies the two actions offered by the card's overflow (⋮) menu.
/// An enum instead of raw strings keeps the menu type-safe.
enum ProductCardAction { edit, delete }

/// One row in the product list.
///
/// Stateless and controlled: it is given a [product] and three callbacks, and
/// it only reports events upward ([onTap], [onEdit], [onDelete]). It never
/// changes any data itself. Because the list gives each card a stable key and
/// this card holds no state, rebuilding one product rebuilds only its row.
class ProductCard extends StatelessWidget {
  const ProductCard({
    super.key,
    required this.product,
    required this.onTap,
    required this.onEdit,
    required this.onDelete,
  });

  final CosmeticProduct product;

  /// Open the read-only detail screen for this product.
  final VoidCallback onTap;

  /// Open the edit screen for this product.
  final VoidCallback onEdit;

  /// Ask to delete this product (the parent shows the confirmation dialog).
  final VoidCallback onDelete;

  /// Size of the circular category icon badge.
  static const double _iconBadgeSize = 48;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      // The card background comes from the theme (our grey #A8A8A8).
      color: theme.colorScheme.surfaceContainerHighest,
      clipBehavior: Clip.antiAlias, // clip the ripple to the rounded corners
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              // Leading category icon. No coloured circle behind it — the icon
              // is tinted to the on-card colour so it blends into the card.
              CategoryIcon(category: product.category, size: _iconBadgeSize),
              const SizedBox(width: 12),

              // The middle column takes the remaining width (`Expanded`).
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      product.name,
                      style: theme.textTheme.titleMedium,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis, // "…" if too long
                    ),
                    if (product.description.isNotEmpty)
                      Text(
                        product.description,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    Text(
                      expiryStatusText(product),
                      style: theme.textTheme.bodySmall?.copyWith(
                        // Expired items show their status in the error colour.
                        color: product.isExpired
                            ? theme.colorScheme.error
                            : theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),

              // Trailing column: opened/unopened label + sync icon + ⋮ menu.
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      // "Waiting to sync" indicator from the offline story.
                      if (!product.isSynced)
                        Icon(
                          Icons.sync,
                          size: _syncIconSize,
                          color: theme.colorScheme.outline,
                        ),
                      // The overflow menu. `PopupMenuButton` shows a small menu
                      // of actions; `onSelected` fires with the chosen enum.
                      PopupMenuButton<ProductCardAction>(
                        onSelected: (action) {
                          switch (action) {
                            case ProductCardAction.edit:
                              onEdit();
                            case ProductCardAction.delete:
                              onDelete();
                          }
                        },
                        itemBuilder: (context) => const [
                          PopupMenuItem(
                            value: ProductCardAction.edit,
                            child: ListTile(
                              leading: Icon(Icons.edit),
                              title: Text('Edit'),
                            ),
                          ),
                          PopupMenuItem(
                            value: ProductCardAction.delete,
                            child: ListTile(
                              leading: Icon(Icons.delete),
                              title: Text('Delete'),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  Text(
                    product.isOpened ? 'Opened' : 'Unopened',
                    style: theme.textTheme.labelMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// Size of the small "waiting to sync" icon.
  static const double _syncIconSize = 16;
}
