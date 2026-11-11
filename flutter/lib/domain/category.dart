// This file defines the product categories the user can choose from.
//
// In Dart an `enum` is a fixed, named set of values. Here each category also
// carries extra data: a human-readable [label] and the [assetPath] of its PNG
// icon. This "enhanced enum" (enum members with fields + a constructor) is the
// same pattern the professor uses for his `SpotCategory`.

/// The folder (declared as an asset in pubspec.yaml) that holds the category
/// PNG icons. Kept as a constant so the path is written once, not repeated on
/// every member (avoids "magic strings").
const String _categoryAssetFolder = 'assets/categories';

/// A closed set of product categories.
///
/// To add or remove a category, edit this enum and drop the matching PNG into
/// assets/categories/. Everything in the UI (the dropdown, the filter chips,
/// the row icon) reads from `CosmeticCategory.values`, so it updates itself.
enum CosmeticCategory {
  // Each line is one enum member. The part in parentheses calls the constructor
  // below, passing the label shown to the user and the path to its icon file.
  // The PNG file names match the ones you add to assets/categories/.
  hair('Hair', '$_categoryAssetFolder/hair.png'),
  body('Body', '$_categoryAssetFolder/cream.png'),
  face('Face', '$_categoryAssetFolder/facecream.png'),
  skincare('Skincare', '$_categoryAssetFolder/skincare.png'),
  makeup('Makeup', '$_categoryAssetFolder/cosmetics.png'),
  fragrance('Fragrance', '$_categoryAssetFolder/parfum.png'),
  other('Other', '$_categoryAssetFolder/other.png');

  // `const` constructor: enum members are compile-time constants, so the
  // constructor must be const. It just stores the two values into the fields.
  const CosmeticCategory(this.label, this.assetPath);

  /// The text shown to the user (e.g. in the dropdown and the filter chips).
  final String label;

  /// The path of this category's PNG icon inside the bundled assets.
  final String assetPath;
}
