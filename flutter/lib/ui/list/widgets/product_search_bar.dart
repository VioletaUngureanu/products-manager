import 'package:flutter/material.dart';

/// A stateless search box for the list screen.
///
/// Controlled by the parent: it shows [query] and reports typing through
/// [onQueryChange]; it keeps no state of its own. The parent stores the query
/// in the notifier (not in a widget variable), so the search text survives
/// screen rebuilds — the lectures' "state up" rule.
class ProductSearchBar extends StatelessWidget {
  const ProductSearchBar({
    super.key,
    required this.query,
    required this.onQueryChange,
  });

  final String query;
  final ValueChanged<String> onQueryChange;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
      child: TextField(
        // Keep the text field's content in sync with [query]. Rebuilding a
        // controller each time is fine here because the field is simple and the
        // query lives in the notifier.
        controller: TextEditingController(text: query)
          // Put the cursor at the end so typing feels natural after a rebuild.
          ..selection = TextSelection.collapsed(offset: query.length),
        onChanged: onQueryChange,
        decoration: InputDecoration(
          hintText: 'Search …',
          prefixIcon: const Icon(Icons.search),
          // Show a clear button only when there is text to clear.
          suffixIcon: query.isEmpty
              ? null
              : IconButton(
                  icon: const Icon(Icons.clear),
                  onPressed: () => onQueryChange(''),
                ),
          border: const OutlineInputBorder(),
          isDense: true,
        ),
      ),
    );
  }
}
