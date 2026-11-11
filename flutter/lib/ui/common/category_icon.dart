import 'package:flutter/material.dart';

import '../../domain/category.dart';

/// Shows a category's PNG icon, tinted to blend into the grey palette.
///
/// Kept as one small reusable widget so the list row, the detail header and the
/// dropdown all render category icons the same way. If the PNG treatment needs
/// to change, it changes here once.
///
/// The PNGs are transparent line-art, so applying a `color` recolours just the
/// drawn lines (not a background), letting them match the surrounding text.
class CategoryIcon extends StatelessWidget {
  const CategoryIcon({
    super.key,
    required this.category,
    this.size = defaultSize,
    this.color,
  });

  /// Which category's icon to show.
  final CosmeticCategory category;

  /// Width/height of the icon in logical pixels.
  final double size;

  /// Optional tint. When null we fall back to the theme's on-surface-variant
  /// colour (the same near-black used for card text), so the icon reads as part
  /// of the palette.
  final Color? color;

  /// Default icon size, as a named constant rather than a magic number.
  static const double defaultSize = 32;

  @override
  Widget build(BuildContext context) {
    // `Theme.of(context)` reads the current app theme (colours, text styles).
    final tint = color ?? Theme.of(context).colorScheme.onSurfaceVariant;

    // `Image.asset` loads a bundled image by its path (declared in pubspec.yaml).
    return Image.asset(
      category.assetPath,
      width: size,
      height: size,
      // `color` + the default blend mode recolours the opaque pixels of the
      // transparent line-art to [tint].
      color: tint,
      // If a PNG file is missing, show a neutral placeholder icon instead of
      // crashing — useful until the real PNGs are dropped into assets/.
      errorBuilder: (context, error, stackTrace) =>
          Icon(Icons.image_not_supported_outlined, size: size, color: tint),
    );
  }
}
