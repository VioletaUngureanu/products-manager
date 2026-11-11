// Unit tests for the pure `filterProducts` function.
//
// These prove the filtering logic works without running the app or a device —
// the point of keeping that logic in a pure function (lectures: "push logic
// into pure functions"). Run them with `flutter test`.

import 'package:cosmetic_manager/domain/category.dart';
import 'package:cosmetic_manager/domain/cosmetic_product.dart';
import 'package:cosmetic_manager/domain/product_filter.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  // A small helper to build a product with only the fields a test cares about.
  CosmeticProduct product({
    required String id,
    required String name,
    required CosmeticCategory category,
    DateTime? openedDate,
    int shelfLifeMonths = 12,
    String description = '',
  }) {
    return CosmeticProduct(
      id: id,
      name: name,
      category: category,
      openedDate: openedDate,
      shelfLifeMonths: shelfLifeMonths,
      description: description,
    );
  }

  // Build dates relative to today so expiry-based tests are stable.
  final today = DateTime.now();
  DateTime monthsAgo(int months) =>
      DateTime(today.year, today.month - months, today.day);

  // A fixed sample covering each read-view state.
  final sample = [
    product(id: '1', name: 'Face Cleanser', category: CosmeticCategory.face),
    product(
      id: '2',
      name: 'Shampoo',
      category: CosmeticCategory.hair,
      openedDate: today.subtract(const Duration(days: 10)),
      shelfLifeMonths: 12,
    ),
    product(
      id: '3',
      name: 'Vitamin C Serum',
      category: CosmeticCategory.skincare,
      openedDate: monthsAgo(11),
      shelfLifeMonths: 12, // about to expire (~1 month left)
    ),
    product(
      id: '4',
      name: 'Old Cream',
      category: CosmeticCategory.body,
      openedDate: monthsAgo(13),
      shelfLifeMonths: 6, // already expired
    ),
  ];

  List<String> ids(List<CosmeticProduct> list) =>
      list.map((p) => p.id).toList();

  test('ALL with an empty query returns every product', () {
    final result = filterProducts(sample, query: '', filter: ProductFilter.all);
    expect(result.length, 4);
  });

  test('UNOPENED returns only products without an opened date', () {
    final result =
        filterProducts(sample, query: '', filter: ProductFilter.unopened);
    expect(ids(result), ['1']);
  });

  test('OPENED returns only products with an opened date', () {
    final result =
        filterProducts(sample, query: '', filter: ProductFilter.opened);
    expect(ids(result), ['2', '3', '4']);
  });

  test('ABOUT_TO_EXPIRE returns opened products near or past expiry', () {
    final result =
        filterProducts(sample, query: '', filter: ProductFilter.aboutToExpire);
    expect(ids(result).toSet(), {'3', '4'});
  });

  test('query is trimmed and case-insensitive, matching the name', () {
    final result =
        filterProducts(sample, query: '  shampoo ', filter: ProductFilter.all);
    expect(ids(result), ['2']);
  });

  test('category narrows the result', () {
    final result = filterProducts(
      sample,
      query: '',
      filter: ProductFilter.all,
      category: CosmeticCategory.hair,
    );
    expect(ids(result), ['2']);
  });

  test('filter and category combine', () {
    final result = filterProducts(
      sample,
      query: '',
      filter: ProductFilter.opened,
      category: CosmeticCategory.skincare,
    );
    expect(ids(result), ['3']);
  });
}
