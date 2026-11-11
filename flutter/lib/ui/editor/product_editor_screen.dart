import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../common/category_dropdown.dart';
import '../common/date_picker_field.dart';
import '../common/shelf_life_picker.dart';
import 'product_editor_notifier.dart';

/// The Create / Edit screen — one screen reused for both operations.
///
/// It is a SEPARATE component from the list (a separate route/screen), which is
/// what the Create and Update rubrics require. Create vs edit is decided by
/// [productId]: null means create, a real id means edit (and the form arrives
/// pre-filled). Every field has a visible label, and validation errors are
/// shown in place on the offending field.
class ProductEditorScreen extends ConsumerWidget {
  const ProductEditorScreen({super.key, this.productId});

  /// The id of the product being edited, or null when creating a new one.
  final String? productId;

  /// Vertical gap between form fields. Named constant, not a magic number.
  static const double _fieldGap = 16;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Watch this product's editor state (the `.family` keyed by productId).
    final state = ref.watch(productEditorProvider(productId));
    final notifier = ref.read(productEditorProvider(productId).notifier);

    return Scaffold(
      appBar: AppBar(title: Text(state.screenTitle)),
      body: SingleChildScrollView(
        // Scrollable so the keyboard can never hide the lower fields.
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // --- Name (required) ---
            const _FieldLabel('Name *'),
            TextField(
              controller: _controllerFor(state.name),
              onChanged: notifier.setName,
              decoration: InputDecoration(
                border: const OutlineInputBorder(),
                // Showing the error here keeps validation in this component.
                errorText: state.nameError,
              ),
            ),
            const SizedBox(height: _fieldGap),

            // --- Description (optional) ---
            const _FieldLabel('Description'),
            TextField(
              controller: _controllerFor(state.description),
              onChanged: notifier.setDescription,
              minLines: 3,
              maxLines: 5,
              decoration: const InputDecoration(border: OutlineInputBorder()),
            ),
            const SizedBox(height: _fieldGap),

            // --- Category (required) ---
            const _FieldLabel('Category *'),
            CategoryDropdown(
              value: state.category,
              onChanged: notifier.setCategory,
              errorText: state.categoryError,
            ),
            const SizedBox(height: _fieldGap),

            // --- Shelf life (required) ---
            const _FieldLabel('Shelf life *'),
            ShelfLifePicker(
              value: state.shelfLifeMonths,
              onChanged: notifier.setShelfLifeMonths,
              errorText: state.shelfLifeError,
            ),
            const SizedBox(height: _fieldGap),

            // --- Bought date (optional) ---
            const _FieldLabel('Bought date'),
            DatePickerField(
              label: 'Bought on',
              value: state.boughtDate,
              onChanged: notifier.setBoughtDate,
            ),
            const SizedBox(height: _fieldGap),

            // --- Opened date (optional, write-once) ---
            const _FieldLabel('Opened date'),
            DatePickerField(
              label: 'Opened on',
              value: state.openedDate,
              onChanged: notifier.setOpenedDate,
              enabled: !state.openedDateLocked,
              helperText: state.openedDateLocked
                  ? "Opened date can't be changed once set."
                  : null,
            ),
            const SizedBox(height: _fieldGap),

            // --- Ingredients (optional) ---
            const _FieldLabel('Ingredients'),
            TextField(
              controller: _controllerFor(state.ingredients),
              onChanged: notifier.setIngredients,
              minLines: 3,
              maxLines: 5,
              decoration: const InputDecoration(border: OutlineInputBorder()),
            ),
            const SizedBox(height: _fieldGap),

            // --- Submit button: "Add" when creating, "Save" when editing ---
            SizedBox(
              width: double.infinity, // stretch full width
              child: FilledButton(
                onPressed: () {
                  // Try to save. On success (valid form) go back to the list;
                  // on failure the notifier has set the field errors and we
                  // stay on the form.
                  final saved = notifier.submit();
                  if (saved) Navigator.of(context).pop();
                },
                child: Text(state.submitButtonLabel),
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Builds a text controller whose text and cursor are set to [text].
  ///
  /// A `TextEditingController` holds the text shown inside a `TextField`.
  /// Rebuilding one each time keeps the field in sync with the notifier's
  /// state, and we place the cursor at the end so typing continues naturally.
  TextEditingController _controllerFor(String text) {
    return TextEditingController(text: text)
      ..selection = TextSelection.collapsed(offset: text.length);
  }
}

/// A small bold label shown above each input field. Private because only the
/// editor screen uses it.
class _FieldLabel extends StatelessWidget {
  const _FieldLabel(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Text(
        text,
        style: Theme.of(context).textTheme.labelLarge?.copyWith(
          color: Theme.of(context).colorScheme.onSurfaceVariant,
        ),
      ),
    );
  }
}
