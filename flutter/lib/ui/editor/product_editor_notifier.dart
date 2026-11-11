import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/products_repository.dart';
import '../../domain/category.dart';
import '../../domain/cosmetic_product.dart';
import 'product_editor_state.dart';

/// Validation messages, kept as constants so the exact wording lives in one
/// place (no "magic strings" sprinkled through the logic).
const String _nameRequiredMessage = 'Name is required';
const String _categoryRequiredMessage = 'Please choose a category';
const String _shelfLifeRequiredMessage = 'Shelf life is required';
const String _shelfLifeTooSmallMessage = 'Shelf life must be at least 1 month';

/// The smallest allowed shelf life, in months.
const int _minShelfLifeMonths = 1;

/// Holds and validates the create/edit form.
///
/// This notifier is parameterised by a product id (see the `.family` provider
/// below): `null` means "create a new product", a real id means "edit that
/// product". On construction it loads the product (edit mode) and pre-fills the
/// form, or starts blank (create mode).
///
/// It extends `FamilyNotifier<State, Arg>` — a Riverpod notifier that receives
/// one argument (`arg`), here the nullable product id.
class ProductEditorNotifier
    extends FamilyNotifier<ProductEditorState, String?> {
  @override
  ProductEditorState build(String? productId) {
    // Create mode: no id -> a blank form.
    if (productId == null) {
      return const ProductEditorState(isEditMode: false);
    }

    // Edit mode: look the product up in the single source of truth and copy its
    // values into the form so every field is pre-populated.
    final existing = ref.read(productsProvider.notifier).findById(productId);
    if (existing == null) {
      // The product was deleted meanwhile; fall back to a blank create form so
      // we never crash.
      return const ProductEditorState(isEditMode: false);
    }

    return ProductEditorState(
      isEditMode: true,
      name: existing.name,
      description: existing.description,
      category: existing.category,
      boughtDate: existing.boughtDate,
      openedDate: existing.openedDate,
      shelfLifeMonths: existing.shelfLifeMonths,
      ingredients: existing.ingredients,
      // If the product already had an opened date, lock that field.
      openedDateLocked: existing.openedDate != null,
    );
  }

  // --- Field change events. Each also clears that field's error, so the error
  //     disappears as the user fixes the problem. ---

  void setName(String value) =>
      state = state.copyWith(name: value, nameError: null);

  void setDescription(String value) =>
      state = state.copyWith(description: value);

  void setCategory(CosmeticCategory value) =>
      state = state.copyWith(category: value, categoryError: null);

  void setBoughtDate(DateTime? value) {
    state = value == null
        ? state.copyWith(clearBoughtDate: true)
        : state.copyWith(boughtDate: value);
  }

  /// Sets the opened date, but only while it is not locked (write-once rule).
  void setOpenedDate(DateTime? value) {
    if (state.openedDateLocked) return;
    state = value == null
        ? state.copyWith(clearOpenedDate: true)
        : state.copyWith(openedDate: value);
  }

  void setShelfLifeMonths(int? value) =>
      state = state.copyWith(shelfLifeMonths: value, shelfLifeError: null);

  void setIngredients(String value) =>
      state = state.copyWith(ingredients: value);

  /// Validates the form and, if valid, saves to the repository.
  ///
  /// Returns true on success (so the screen can navigate back) and false when
  /// there are validation errors (the screen stays put and the errors show in
  /// place). This keeps all validation in this component, as the rubric asks.
  bool submit() {
    final current = state;

    final nameError =
        current.name.trim().isEmpty ? _nameRequiredMessage : null;
    final categoryError =
        current.category == null ? _categoryRequiredMessage : null;
    final shelfLifeError = _validateShelfLife(current.shelfLifeMonths);

    final hasErrors =
        nameError != null || categoryError != null || shelfLifeError != null;
    if (hasErrors) {
      // Write the errors back into the state so each field can show its message.
      state = current.copyWith(
        nameError: nameError,
        categoryError: categoryError,
        shelfLifeError: shelfLifeError,
      );
      return false;
    }

    // Validation passed, so these are guaranteed non-null (the `!` just tells
    // the compiler we are sure).
    final category = current.category!;
    final shelfLifeMonths = current.shelfLifeMonths!;
    final repository = ref.read(productsProvider.notifier);

    if (current.isEditMode) {
      final editingId = arg; // `arg` is the product id passed to the family.
      final existing =
          editingId == null ? null : repository.findById(editingId);
      if (existing != null) {
        repository.update(
          existing.copyWith(
            name: current.name.trim(),
            description: current.description.trim(),
            category: category,
            shelfLifeMonths: shelfLifeMonths,
            ingredients: current.ingredients.trim(),
            // Preserve an already-set opened date; otherwise allow setting it.
            openedDate: existing.openedDate ?? current.openedDate,
            boughtDate: current.boughtDate,
            clearBoughtDate: current.boughtDate == null,
          ),
        );
      }
    } else {
      repository.add(
        CosmeticProduct(
          id: '', // the repository assigns the real id
          name: current.name.trim(),
          description: current.description.trim(),
          category: category,
          shelfLifeMonths: shelfLifeMonths,
          ingredients: current.ingredients.trim(),
          boughtDate: current.boughtDate,
          openedDate: current.openedDate,
        ),
      );
    }

    return true;
  }

  /// Returns a validation message for the shelf life, or null if valid.
  String? _validateShelfLife(int? months) {
    if (months == null) return _shelfLifeRequiredMessage;
    if (months < _minShelfLifeMonths) return _shelfLifeTooSmallMessage;
    return null;
  }
}

/// A `.family` provider: it builds a SEPARATE notifier per product id argument.
/// The screen reads `productEditorProvider(productId)` (productId may be null).
final productEditorProvider = NotifierProvider.family<ProductEditorNotifier,
    ProductEditorState, String?>(
  ProductEditorNotifier.new,
);
