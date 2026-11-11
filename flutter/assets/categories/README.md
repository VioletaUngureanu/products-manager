# Category icons

Put the transparent-background category PNG files here. The file names must
match the paths declared in `lib/domain/category.dart`:

| Category  | Expected file name |
|-----------|--------------------|
| Hair      | `hair.png`         |
| Body      | `cream.png`        |
| Face      | `facecream.png`    |
| Skincare  | `skincare.png`     |
| Makeup    | `cosmetics.png`    |
| Fragrance | `parfum.png`       |
| Other     | `other.png`        |

These are the same PNGs used by the native Android app. They are declared as
app assets in `pubspec.yaml` under `assets/categories/`.

Until the real files are added, the app shows a neutral placeholder icon in
their place (see `CategoryIcon` in `lib/ui/common/category_icon.dart`), so it
still runs.
