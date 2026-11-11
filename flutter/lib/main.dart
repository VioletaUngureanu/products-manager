import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'ui/list/product_list_screen.dart';

/// The two anchor colours from the original design, reused from the native
/// Android app so both versions look the same:
///  * cards:   #A8A8A8
///  * buttons: #747474
/// Defined as constants (no magic colour literals scattered around).
const Color _cardGrey = Color(0xFFA8A8A8);
const Color _buttonGrey = Color(0xFF747474);
const Color _onDark = Color(0xFFFFFFFF); // text/icons on the dark-grey buttons
const Color _onLight = Color(0xFF1C1C1C); // text/icons on light surfaces
const Color _onCard = Color(0xFF1F1F1F); // text/icons on the grey cards
const Color _surface = Color(0xFFF5F5F5); // near-white app surface

/// `main` is the program entry point. `runApp` puts the given widget on screen.
/// We wrap the whole app in a `ProviderScope`, which Riverpod requires: it is
/// the storage that holds every provider's state for the app's lifetime.
void main() {
  runApp(const ProviderScope(child: CosmeticManagerApp()));
}

/// The root widget of the application.
class CosmeticManagerApp extends StatelessWidget {
  const CosmeticManagerApp({super.key});

  @override
  Widget build(BuildContext context) {
    // Build a Material 3 colour scheme from our greys. We start from a seed and
    // then override the specific roles the UI uses:
    //  * primary        -> buttons / FAB            (#747474)
    //  * surfaceContainerHighest -> card background  (#A8A8A8)
    // Dynamic/wallpaper colour is NOT used, so the grey palette always applies.
    final colorScheme = ColorScheme.fromSeed(
      seedColor: _buttonGrey,
      brightness: Brightness.light,
    ).copyWith(
      primary: _buttonGrey,
      onPrimary: _onDark,
      surface: _surface,
      onSurface: _onLight,
      surfaceContainerHighest: _cardGrey, // the colour ProductCard reads
      onSurfaceVariant: _onCard,
    );

    return MaterialApp(
      title: 'CosmeticManager',
      // Hide the little "debug" ribbon in the top-right corner.
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: colorScheme,
        useMaterial3: true,
      ),
      // The first screen shown when the app starts.
      home: const ProductListScreen(),
    );
  }
}
