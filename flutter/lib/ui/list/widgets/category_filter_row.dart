import 'package:flutter/material.dart';

import '../../../domain/category.dart';
import '../../common/category_icon.dart';

/// A horizontally-scrolling row of category filter chips ("Categories" strip).
///
/// Stateless/controlled: the [selected] category and the [onSelected] callback
/// are passed in. Tapping the already-selected chip clears the filter (the
/// toggle logic lives in the notifier).
class CategoryFilterRow extends StatelessWidget {
  const CategoryFilterRow({
    super.key,
    required this.selected,
    required this.onSelected,
  });

  /// The currently active category filter, or null for "all".
  final CosmeticCategory? selected;

  /// Called with the tapped category.
  final ValueChanged<CosmeticCategory> onSelected;

  /// Icon size shown on each chip.
  static const double _chipIconSize = 18;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      // A fixed height so the horizontal list has bounded vertical space.
      height: _rowHeight,
      child: ListView(
        // Lay the chips out left-to-right and let them scroll horizontally.
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        children: [
          for (final category in CosmeticCategory.values)
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: FilterChip(
                selected: selected == category,
                onSelected: (_) => onSelected(category),
                avatar: CategoryIcon(
                  category: category,
                  size: _chipIconSize,
                  // Tint the icon to match the chip's label colour so it stays
                  // readable whether the chip is selected or not.
                  color: selected == category
                      ? Theme.of(context).colorScheme.onSecondaryContainer
                      : Theme.of(context).colorScheme.onSurfaceVariant,
                ),
                label: Text(category.label),
              ),
            ),
        ],
      ),
    );
  }

  /// Fixed height for the chip strip.
  static const double _rowHeight = 56;
}
