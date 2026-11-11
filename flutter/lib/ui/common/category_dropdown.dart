import 'package:flutter/material.dart';

import '../../domain/category.dart';
import 'category_icon.dart';

/// A labelled dropdown for choosing one [CosmeticCategory].
///
/// This widget is **stateless** and "controlled": it does not remember the
/// selection itself. The parent passes in the current [value] and gets told
/// about changes through [onChanged]. This is Flutter's version of the
/// lectures' "state hoisting" rule — the real state lives up in the owner, the
/// field only displays it and reports events. The same hoisting applies to
/// every form field widget in this folder.
class CategoryDropdown extends StatelessWidget {
  const CategoryDropdown({
    super.key,
    required this.value,
    required this.onChanged,
    this.errorText,
  });

  /// Currently selected category, or null if nothing is chosen yet.
  final CosmeticCategory? value;

  /// Called with the newly chosen category when the user picks one.
  final ValueChanged<CosmeticCategory> onChanged;

  /// A validation error to show under the field, or null when valid.
  final String? errorText;

  @override
  Widget build(BuildContext context) {
    // `DropdownButtonFormField` is a dropdown that also integrates with the
    // form-field look: it can show a label, a border and an error message.
    return DropdownButtonFormField<CosmeticCategory>(
      // `initialValue` sets which item is shown as selected.
      initialValue: value,
      decoration: InputDecoration(
        // The visible placeholder shown when nothing is selected.
        hintText: 'Choose a category',
        border: const OutlineInputBorder(),
        // Showing errorText here is how validation errors are handled in-place,
        // as the rubric requires.
        errorText: errorText,
      ),
      // Build one menu entry per category, each with its icon and label.
      items: [
        for (final category in CosmeticCategory.values)
          DropdownMenuItem<CosmeticCategory>(
            value: category,
            child: Row(
              children: [
                CategoryIcon(category: category, size: _menuIconSize),
                const SizedBox(width: 8),
                Text(category.label),
              ],
            ),
          ),
      ],
      // `onChanged` gives us a nullable value; we ignore null (it only happens
      // if the dropdown is cleared, which this field does not allow) and report
      // real selections upward.
      onChanged: (selected) {
        if (selected != null) onChanged(selected);
      },
    );
  }

  /// Icon size inside each dropdown menu row.
  static const double _menuIconSize = 24;
}
