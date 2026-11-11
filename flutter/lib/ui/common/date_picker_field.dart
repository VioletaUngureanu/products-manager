import 'package:flutter/material.dart';

import 'date_formatting.dart';

/// A read-only, labelled field that opens a calendar date picker when tapped.
///
/// Controlled/stateless with respect to the value: the parent passes the
/// current [value] and receives changes via [onChanged] (state hoisting again).
/// When [enabled] is false the field is locked and does not open the picker —
/// used for the write-once "opened date" once it has been set.
class DatePickerField extends StatelessWidget {
  const DatePickerField({
    super.key,
    required this.label,
    required this.value,
    required this.onChanged,
    this.enabled = true,
    this.helperText,
  });

  /// Text shown inside the field as its label (e.g. "Bought on").
  final String label;

  /// The currently selected date, or null if none.
  final DateTime? value;

  /// Called with the new date, or null when the user clears it.
  final ValueChanged<DateTime?> onChanged;

  /// When false the field is greyed out and tapping does nothing.
  final bool enabled;

  /// Optional hint shown under the field (e.g. the write-once explanation).
  final String? helperText;

  /// How many years into the past/future the calendar can scroll. A named
  /// constant instead of a magic number.
  static const int selectableYearSpan = 10;

  @override
  Widget build(BuildContext context) {
    // `InkWell` makes its child tappable and shows a ripple. We disable the tap
    // when the field is locked by passing a null onTap.
    return InkWell(
      onTap: enabled ? () => _pickDate(context) : null,
      child: InputDecorator(
        // `InputDecorator` draws the label/border/helper around custom content,
        // so a non-text widget can still look like a form field.
        decoration: InputDecoration(
          labelText: label,
          border: const OutlineInputBorder(),
          helperText: helperText,
          prefixIcon: const Icon(Icons.calendar_today),
          // A small clear button on the right, only when there is a value to
          // clear and the field is editable.
          suffixIcon: (value != null && enabled)
              ? IconButton(
                  icon: const Icon(Icons.clear),
                  onPressed: () => onChanged(null),
                )
              : null,
        ),
        child: Text(formatDateOrDash(value)),
      ),
    );
  }

  /// Opens the Material calendar dialog and reports the chosen date upward.
  Future<void> _pickDate(BuildContext context) async {
    final now = DateTime.now();
    // `showDatePicker` returns a Future that completes with the chosen date, or
    // null if the user cancelled. `await` waits for that result.
    final picked = await showDatePicker(
      context: context,
      initialDate: value ?? now,
      firstDate: DateTime(now.year - selectableYearSpan),
      lastDate: DateTime(now.year + selectableYearSpan),
    );
    if (picked != null) onChanged(picked);
  }
}
