import 'category.dart';

/// The single domain entity of the app: one cosmetic product.
///
/// Design choices (same as the native Android version):
///  * It is **immutable** — every field is `final`, so once an object is built
///    its values never change. To "edit" a product we build a new copy with
///    [copyWith]. Immutability is what makes the single-source-of-truth and
///    unidirectional-data-flow rules from the lectures hold: the state cannot be
///    changed behind the UI's back.
///  * Stored data lives in the constructor; values that can be *derived* from
///    that data (like the expiry date) are exposed as **getters** that compute
///    on the fly, so they can never drift out of sync with the stored fields.
class CosmeticProduct {
  // `const` constructor: when every argument is a compile-time constant, Dart
  // can build the object at compile time (cheaper, and lets us mark seed data
  // `const`). Named parameters ({ ... }) make call sites self-documenting.
  const CosmeticProduct({
    required this.id,
    required this.name,
    required this.category,
    required this.shelfLifeMonths,
    this.description = '',
    this.boughtDate,
    this.openedDate,
    this.ingredients = '',
    this.lastModified,
    this.isSynced = false,
  });

  // --- Internal fields (housekeeping, not shown as editable inputs) ---

  /// Unique identifier. The repository assigns this, not the UI.
  final String id;

  /// When the product was last changed. Null for seed data that was never
  /// edited locally. Kept for a future server-sync feature.
  final DateTime? lastModified;

  /// Whether this product has been sent to a server. Always false for locally
  /// created/edited items in this in-memory milestone; drives the small
  /// "waiting to sync" icon on the list row.
  final bool isSynced;

  // --- External fields (the user-facing product data) ---

  /// Required. The product name, e.g. "Vitamin C Serum".
  final String name;

  /// Optional free-text description. Defaults to empty.
  final String description;

  /// Required. Which category the product belongs to.
  final CosmeticCategory category;

  /// Optional. The date the product was bought.
  final DateTime? boughtDate;

  /// Optional, and **write-once**: once set it must not change, so the app can
  /// reliably compute how long an opened product stays safe to use.
  final DateTime? openedDate;

  /// Required. How many months the product lasts once opened.
  final int shelfLifeMonths;

  /// Optional free-text ingredient list. Defaults to empty.
  final String ingredients;

  // --- Computed properties (derived from the stored fields above) ---

  /// A product counts as "opened" once an opened date has been set.
  bool get isOpened => openedDate != null;

  /// The date an opened product stops being safe to use: opened date plus the
  /// shelf life in months. Null while the product is still unopened, because
  /// shelf life only starts counting once the product is opened.
  DateTime? get expiryDate {
    final opened = openedDate;
    if (opened == null) return null;
    // DateTime has no "+N months" helper, so we build a new date with the month
    // advanced by shelfLifeMonths. Dart normalises overflow automatically
    // (e.g. month 13 becomes January of the next year).
    return DateTime(opened.year, opened.month + shelfLifeMonths, opened.day);
  }

  /// Whole days from today until expiry. Negative when already expired.
  /// Null while the product is unopened.
  int? get daysUntilExpiry {
    final expiry = expiryDate;
    if (expiry == null) return null;
    final now = DateTime.now();
    // Compare date-only values so the result is a clean number of days,
    // unaffected by the current time of day.
    final today = DateTime(now.year, now.month, now.day);
    final expiryDay = DateTime(expiry.year, expiry.month, expiry.day);
    return expiryDay.difference(today).inDays;
  }

  /// True once an opened product is past its expiry date.
  bool get isExpired {
    final days = daysUntilExpiry;
    return days != null && days < 0;
  }

  /// "About to expire": the product was opened and its shelf life is almost
  /// over (within [aboutToExpireThresholdDays] days, or already expired).
  bool get isAboutToExpire {
    final days = daysUntilExpiry;
    return days != null && days <= aboutToExpireThresholdDays;
  }

  /// Builds a new product that is a copy of this one, with the given fields
  /// replaced. This is how we "change" an immutable object.
  ///
  /// Note the subtlety with the three nullable fields (boughtDate, openedDate,
  /// lastModified): a plain `value ?? this.value` could never set them back to
  /// null. We use a sentinel pattern so callers can explicitly clear them.
  CosmeticProduct copyWith({
    String? id,
    String? name,
    CosmeticCategory? category,
    int? shelfLifeMonths,
    String? description,
    String? ingredients,
    bool? isSynced,
    // These flags let a caller say "please set this date field to null".
    DateTime? boughtDate,
    bool clearBoughtDate = false,
    DateTime? openedDate,
    bool clearOpenedDate = false,
    DateTime? lastModified,
    bool clearLastModified = false,
  }) {
    return CosmeticProduct(
      id: id ?? this.id,
      name: name ?? this.name,
      category: category ?? this.category,
      shelfLifeMonths: shelfLifeMonths ?? this.shelfLifeMonths,
      description: description ?? this.description,
      ingredients: ingredients ?? this.ingredients,
      isSynced: isSynced ?? this.isSynced,
      boughtDate: clearBoughtDate ? null : (boughtDate ?? this.boughtDate),
      openedDate: clearOpenedDate ? null : (openedDate ?? this.openedDate),
      lastModified:
          clearLastModified ? null : (lastModified ?? this.lastModified),
    );
  }

  /// How close to expiry counts as "about to expire". A named constant instead
  /// of a magic number, so the rule is easy to find and change.
  static const int aboutToExpireThresholdDays = 30;
}
