import 'package:flutter/material.dart';

/// Lets the user pick a shelf life in months: quick-pick chips for the common
/// values, plus a "Custom" chip that reveals a number field for anything else.
///
/// Like the other form fields it is "controlled": the chosen [value] (in
/// months, or null if nothing chosen yet) and the [onChanged] callback are
/// passed in by the parent. The only thing it remembers locally is whether the
/// custom field is currently shown — that is pure throwaway UI state, which the
/// lectures say is fine to keep inside the widget. Because it keeps that local
/// flag, it must be a `StatefulWidget`.
class ShelfLifePicker extends StatefulWidget {
  const ShelfLifePicker({
    super.key,
    required this.value,
    required this.onChanged,
    this.errorText,
  });

  final int? value;
  final ValueChanged<int?> onChanged;
  final String? errorText;

  /// The preset values offered as quick-pick chips, in months.
  static const List<int> presetMonths = [6, 12, 24];

  /// Max number of digits allowed in the custom months field.
  static const int maxCustomDigits = 3;

  @override
  State<ShelfLifePicker> createState() => _ShelfLifePickerState();
}

// The `State` object holds the widget's mutable local UI state and builds it.
class _ShelfLifePickerState extends State<ShelfLifePicker> {
  // True when the current value is a custom (non-preset) number, so the custom
  // text field should be visible. Initialised from the incoming value.
  late bool _customMode =
      widget.value != null && !ShelfLifePicker.presetMonths.contains(widget.value);

  @override
  Widget build(BuildContext context) {
    // A Column stacks its children vertically.
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // `Wrap` lays the chips out in a row that wraps to the next line if
        // there is not enough width (safer than a fixed Row).
        Wrap(
          spacing: 8,
          children: [
            for (final months in ShelfLifePicker.presetMonths)
              ChoiceChip(
                label: Text('${months}M'),
                // A preset chip is selected when we are NOT in custom mode and
                // the value equals this preset.
                selected: !_customMode && widget.value == months,
                onSelected: (_) {
                  // `setState` tells Flutter this widget's local state changed
                  // so it must rebuild.
                  setState(() => _customMode = false);
                  widget.onChanged(months);
                },
              ),
            // The "Custom" chip reveals the number field.
            ChoiceChip(
              label: const Text('Custom'),
              selected: _customMode,
              onSelected: (_) {
                setState(() => _customMode = true);
                // If the current value is a preset, clear it so the custom
                // field starts empty.
                if (ShelfLifePicker.presetMonths.contains(widget.value)) {
                  widget.onChanged(null);
                }
              },
            ),
          ],
        ),

        // Only build the custom number field when custom mode is on.
        if (_customMode)
          Padding(
            padding: const EdgeInsets.only(top: 8),
            child: TextField(
              // Show the current value as the starting text.
              controller: TextEditingController(
                text: widget.value?.toString() ?? '',
              ),
              keyboardType: TextInputType.number, // numeric keypad
              decoration: const InputDecoration(
                labelText: 'Months',
                border: OutlineInputBorder(),
              ),
              onChanged: (text) {
                // Keep only digits and cap the length, then report the parsed
                // int (or null if the field is empty / invalid).
                var digits = text.replaceAll(RegExp(r'[^0-9]'), '');
                if (digits.length > ShelfLifePicker.maxCustomDigits) {
                  digits = digits.substring(0, ShelfLifePicker.maxCustomDigits);
                }
                widget.onChanged(digits.isEmpty ? null : int.tryParse(digits));
              },
            ),
          ),

        // Show the validation error (if any) under the chips.
        if (widget.errorText != null)
          Padding(
            padding: const EdgeInsets.only(top: 4, left: 4),
            child: Text(
              widget.errorText!,
              style: TextStyle(
                color: Theme.of(context).colorScheme.error,
                fontSize: _errorFontSize,
              ),
            ),
          ),
      ],
    );
  }

  /// Font size for the inline error text.
  static const double _errorFontSize = 12;
}
