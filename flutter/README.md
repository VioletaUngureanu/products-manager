# CosmeticManager — Flutter

A non-native (Flutter) version of CosmeticManager: add, list, view, edit and
delete cosmetic products, with fake in-memory data. It is the same app and the
same domain as the native Android version, built to the same CRUD rubric.

## Tech choices

- **Flutter** + **Dart**, Material 3.
- **Riverpod 2** (`flutter_riverpod`) for state management — the state lives in
  `Notifier`s outside the widgets, exactly as in the course's Flutter example.
- **No database, no network** yet: data is in-memory
  (`lib/data/products_repository.dart`), which is what the assignment asks for
  to demo CRUD.

## Project layout (`lib/`)

```
lib/
├─ main.dart                         app entry point + grey Material theme
├─ domain/                           pure Dart, no Flutter imports
│  ├─ category.dart                  CosmeticCategory enum (+ icon asset path)
│  ├─ cosmetic_product.dart          the immutable product entity + expiry rules
│  └─ product_filter.dart            the 4 read-views + the pure filterProducts()
├─ data/
│  ├─ seed_data.dart                 demo products
│  └─ products_repository.dart       single source of truth (Notifier<List>)
└─ ui/
   ├─ common/                        reusable, controlled form widgets
   ├─ list/                          Read screen + its state + row widgets
   ├─ editor/                        Create/Edit screen + its state/notifier
   └─ detail/                        read-only single-product screen
```

## How to run

This repository contains **only the Dart sources** (no generated build folders),
as the submission rules require. To run it you first generate the platform
runner folders once, then launch:

```bash
cd flutter
flutter create .      # generates android/, ios/, etc. around the existing lib/
flutter pub get       # downloads the pinned dependencies
flutter run           # run on a connected device or emulator
flutter test          # run the unit tests
```

`flutter create .` is safe: it only adds the platform scaffolding and never
overwrites `lib/`, `pubspec.yaml`, `test/` or `assets/`.

## Category icons

Drop the category PNGs into `assets/categories/` — see
`assets/categories/README.md` for the exact file names. Until then the app
shows a neutral placeholder icon so it still runs.

## How the CRUD rubric is met

- **Read (list):** `lib/ui/list/product_list_screen.dart` uses a lazy
  `ListView.builder` driven by Riverpod providers; each row has a stable
  `ValueKey(product.id)`, so create/edit/delete rebuild only the affected row —
  the list and the screen are never rebuilt wholesale.
- **Create:** a separate screen (`lib/ui/editor/`), all main fields, a visible
  label per field, in-place validation; on save only the new item is added.
- **Update:** the same separate screen, pre-populated, the button says **Save**
  (not "Add"); only the matching item is replaced, identified by id.
- **Delete:** a confirmation dialog; only the product id is passed back to the
  repository.
