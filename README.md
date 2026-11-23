# CosmeticManager — Android (Native UI milestone)

An Android app for visualizing and managing cosmetics products: keep track of what you own, when
each product was opened, and when it is about to expire.

This repository is the **UI-only / in-memory milestone** of the project (Lab 2, "UI only — Native").
It implements all four CRUD operations against fake in-memory data, each on its own screen.
Persistence (local DB) and server sync come in later milestones; the model already carries the
fields those will need (`lastModified`, `isSynced`), and a "waiting to sync" indicator is shown as a
placeholder.

The original project brief is kept in [`docs/PROJECT_IDEA.md`](docs/PROJECT_IDEA.md).

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM / UDF**: `ViewModel` + `StateFlow`, immutable `UiState`, unidirectional data flow
- **Navigation Compose** for separate CRUD screens
- No Hilt yet (introduced later in the course) — a tiny `ServiceLocator` shares the single
  repository instance. No Room / networking yet — an `InMemoryProductRepository` is the single
  source of truth.

## How to run

1. Open the project folder in **Android Studio** (a recent stable version).
2. Let it sync Gradle (first sync downloads the Android SDK components if needed).
3. Run the `app` configuration on an emulator or device (minSdk 24).

From the command line:

```bash
./gradlew :app:assembleDebug      # build the debug APK
./gradlew :app:testDebugUnitTest  # run the unit tests
```

## What you can do in the app

- **List ("Your products")** — search, filter by category chip, and switch between the four read
  views: **All / Unopened / Opened / About to expire**.
- **Add** — a separate form screen with labelled fields, category dropdown, shelf-life picker
  (6 / 12 / 24 months or custom), optional bought/opened dates and ingredients, and inline
  validation.
- **Edit** — the same form, pre-populated, with the submit button reading **Save**. The opened date
  is write-once: once set it becomes read-only.
- **Open a product** — a read-only detail screen showing every field.
- **Delete** — from the list row's menu or the detail screen, guarded by a confirmation dialog.

## Architecture

```
app/src/main/java/com/cosmeticmanager/app/
├─ domain/        CosmeticProduct, Category, ProductFilter + pure filterProducts()
├─ data/          ProductRepository (interface) + InMemoryProductRepository + ServiceLocator
└─ ui/
   ├─ theme/      Material 3 theme
   ├─ common/     Reusable, stateless form components (dropdown, date field, shelf-life picker)
   ├─ list/       ProductListScreen + ViewModel + UiState + row/search/chip components
   ├─ editor/     ProductEditorScreen + ViewModel + UiState (shared by create & edit)
   ├─ detail/     ProductDetailScreen (read-only single product)
   └─ navigation/ CosmeticNavHost (list · create · edit · detail destinations)
```

State flows **down** (repository → ViewModel → `UiState` → composables) and events flow **up**
(composables → ViewModel → repository). The list is a `LazyColumn` keyed by product id, so a
create/update/delete only touches the affected row — the list is never rebuilt by hand.

## How each CRUD operation maps to the grading rubric

| Operation | Where | Notes |
|-----------|-------|-------|
| **Read (list)** | `ui/list/ProductListScreen.kt` | `LazyColumn` driven by a `ViewModel`'s `StateFlow` (observed via `collectAsStateWithLifecycle`). Items keyed by id; only the changed item recomposes. |
| **Create** | `ui/editor/` | Separate screen. All main fields, every field labelled, validation errors shown in place. Only the new item is added (via the shared repository). |
| **Update** | `ui/editor/` | Same separate screen, pre-populated, button says **Save**. Product identified by id; only that item changes. |
| **Delete** | list row menu / detail screen | Confirmation dialog; only the id is passed back to remove the item. |

## Tests

`app/src/test/.../ProductFilterTest.kt` unit-tests the pure `filterProducts(query, filter, category)`
helper (search, the four read views, category narrowing) — fast, no device needed.
