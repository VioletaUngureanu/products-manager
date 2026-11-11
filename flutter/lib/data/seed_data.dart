import '../domain/category.dart';
import '../domain/cosmetic_product.dart';

/// A small set of demo products so the list is not empty on first launch.
///
/// This mirrors the seed data in the native Android app. It is a plain
/// top-level `const` list (the professor keeps his seed data the same way, in a
/// `kSeedSpots` constant). The `k` prefix is a common Dart convention meaning
/// "compile-time constant".
///
/// Dates are built relative to `DateTime.now()` so the demo always has products
/// in each state (fresh, opened, about to expire, expired). Because these
/// depend on the current time they cannot be `const`, so the list is `final`.
final List<CosmeticProduct> kSeedProducts = _buildSeedProducts();

List<CosmeticProduct> _buildSeedProducts() {
  final now = DateTime.now();
  final today = DateTime(now.year, now.month, now.day);

  // Small helpers to express dates readably as "N months/days ago".
  DateTime monthsAgo(int months) =>
      DateTime(today.year, today.month - months, today.day);
  DateTime daysAgo(int days) => today.subtract(Duration(days: days));

  return [
    CosmeticProduct(
      id: 'seed-1',
      name: 'Sheabutter Cream',
      description: 'Hydrating body cream for very dry skin.',
      category: CosmeticCategory.body,
      boughtDate: monthsAgo(8),
      openedDate: monthsAgo(6),
      shelfLifeMonths: 6, // opened 6 months ago + 6 months shelf life => expired
      ingredients: 'Shea butter, glycerin, vitamin E',
      isSynced: true,
    ),
    CosmeticProduct(
      id: 'seed-2',
      name: 'Vitamin C Serum',
      description: 'Brightening serum for a glowing complexion.',
      category: CosmeticCategory.skincare,
      boughtDate: monthsAgo(2),
      openedDate: monthsAgo(1),
      shelfLifeMonths: 2, // about to expire soon
      ingredients: 'Ascorbic acid, ferulic acid, vitamin E',
      isSynced: true,
    ),
    CosmeticProduct(
      id: 'seed-3',
      name: 'Shampoo For Fine Hair',
      description: 'Strengthening shampoo that adds volume.',
      category: CosmeticCategory.hair,
      boughtDate: monthsAgo(1),
      openedDate: daysAgo(14),
      shelfLifeMonths: 12, // opened, plenty of life left
      ingredients: 'Water, keratin, panthenol',
      isSynced: true,
    ),
    CosmeticProduct(
      id: 'seed-4',
      name: 'Face Cleanser',
      description: 'Cleans skin and removes makeup gently.',
      category: CosmeticCategory.face,
      boughtDate: daysAgo(3),
      openedDate: null, // never opened -> "Unopened"
      shelfLifeMonths: 24,
      ingredients: 'Water, glycerin, mild surfactants',
      isSynced: true,
    ),
  ];
}
