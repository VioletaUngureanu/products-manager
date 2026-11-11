import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../domain/cosmetic_product.dart';
import 'seed_data.dart';

/// The single source of truth for the whole product list.
///
/// In Riverpod a `Notifier<T>` is a state holder: it owns a value of type `T`
/// (here, the list of products) and lets widgets observe it. When we assign a
/// new value to `state`, Riverpod notifies every widget that is watching, and
/// they rebuild. This is the professor's chosen Flutter pattern (his
/// `SpotListNotifier extends Notifier<SpotListUiState>`), here applied to the
/// data layer so the raw product list has exactly one owner.
///
/// Important: the list is **immutable**. Every CRUD method builds a brand-new
/// list and assigns it to `state`; we never mutate the existing list in place.
/// Assigning a new list instance is what makes Riverpod detect the change and
/// rebuild the UI. This is the lectures' "state as an immutable structure" rule.
class ProductsNotifier extends Notifier<List<CosmeticProduct>> {
  /// `build()` returns the initial state the first time this notifier is read.
  /// We start from the seed products (a copy, so we never mutate the seed list).
  @override
  List<CosmeticProduct> build() => List<CosmeticProduct>.from(kSeedProducts);

  /// Adds a brand-new product.
  ///
  /// The repository (not the UI) assigns the id, the "last modified" timestamp
  /// and the unsynced flag, so the UI cannot create duplicate ids or forget the
  /// housekeeping fields. Returns the stored product (with its generated id) in
  /// case the caller needs it.
  CosmeticProduct add(CosmeticProduct product) {
    final stored = product.copyWith(
      id: _generateId(),
      lastModified: DateTime.now(),
      isSynced: false,
    );
    // `[...state, stored]` is a NEW list: all current items plus the new one.
    state = [...state, stored];
    return stored;
  }

  /// Replaces an existing product (matched by id) with an updated copy.
  /// Only the matching item changes; every other item is kept as-is, so the
  /// list screen rebuilds only the affected row.
  void update(CosmeticProduct product) {
    final stamped = product.copyWith(
      lastModified: DateTime.now(),
      isSynced: false,
    );
    state = [
      for (final current in state)
        if (current.id == stamped.id) stamped else current,
    ];
  }

  /// Removes the product with the given id. `.where(...)` builds a new list
  /// without that item.
  void delete(String id) {
    state = state.where((product) => product.id != id).toList();
  }

  /// Looks up a single product by id, or null if it no longer exists.
  /// Used by the edit and detail screens, which receive only an id.
  CosmeticProduct? findById(String id) {
    for (final product in state) {
      if (product.id == id) return product;
    }
    return null;
  }

  /// Produces a unique id for a new product. We use the current microsecond
  /// count, which is unique enough for an in-memory demo with one user.
  String _generateId() => 'product-${DateTime.now().microsecondsSinceEpoch}';
}

/// The provider that exposes [ProductsNotifier] to the rest of the app.
///
/// A `NotifierProvider<N, T>` creates and holds one shared instance of the
/// notifier `N` whose current value has type `T`. Because every widget reads
/// the SAME provider, they all share the SAME product list — one source of
/// truth across the create, edit, detail and list screens.
///
/// * `ref.watch(productsProvider)`          -> the current List<CosmeticProduct>
///   (rebuilds the widget when the list changes).
/// * `ref.read(productsProvider.notifier)`  -> the notifier, to call add/update/
///   delete (does NOT subscribe the widget to changes).
final productsProvider =
    NotifierProvider<ProductsNotifier, List<CosmeticProduct>>(
      ProductsNotifier.new,
    );
