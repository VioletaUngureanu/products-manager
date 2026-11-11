import 'category.dart';
import 'cosmetic_product.dart';

/// The four "Read" views from the assignment: all / unopened / opened /
/// about-to-expire. An enum keeps the set closed and type-safe (the UI can only
/// ever deal with one of these four), and each member carries the tab [label].
enum ProductFilter {
  all('All'),
  unopened('Unopened'),
  opened('Opened'),
  aboutToExpire('About to expire');

  const ProductFilter(this.label);

  final String label;
}

/// Pure filtering logic, kept as a **top-level function** (not inside a widget
/// or the notifier) on purpose.
///
/// Why: it is "pure" — same inputs always give the same output, with no side
/// effects, no UI and no state. This is the lectures' "push logic into pure
/// functions" rule: such a function can be unit-tested in milliseconds with no
/// device or framework. The list notifier simply calls this; the UI never
/// filters by itself.
///
/// A product is kept only if it matches the search query AND the category AND
/// the selected read-view.
///
/// * [query]    free text, matched against name and description (trimmed,
///              case-insensitive). Empty query matches everything.
/// * [filter]   which of the four read-views to apply.
/// * [category] optional category narrowing; null means "any category".
List<CosmeticProduct> filterProducts(
  List<CosmeticProduct> products, {
  required String query,
  required ProductFilter filter,
  CosmeticCategory? category,
}) {
  final trimmedQuery = query.trim().toLowerCase();

  // `.where(...)` keeps only the elements for which the test returns true.
  // `.toList()` turns the lazy result back into a concrete List.
  return products.where((product) {
    final matchesQuery =
        trimmedQuery.isEmpty ||
        product.name.toLowerCase().contains(trimmedQuery) ||
        product.description.toLowerCase().contains(trimmedQuery);

    final matchesCategory = category == null || product.category == category;

    // `switch` on the enum. Dart requires every enum value to be handled (the
    // analyzer flags a missing case), so adding a 5th filter later forces us to
    // decide its rule here — a built-in safety net against forgotten cases.
    final matchesFilter = switch (filter) {
      ProductFilter.all => true,
      ProductFilter.unopened => !product.isOpened,
      ProductFilter.opened => product.isOpened,
      ProductFilter.aboutToExpire => product.isAboutToExpire,
    };

    return matchesQuery && matchesCategory && matchesFilter;
  }).toList();
}
