# CosmeticManager — Architecture & Design Rationale

This document explains **how the app is put together**, **how data flows through it**, and **why each
decision was made** — with explicit references to the mobile-development lecture principles and to
the grading rubric for the UI assignment.

It is written so that someone who has never seen the code can read this file, then open any screen
and understand what it does and why.

---

## 1. What the app is

**CosmeticManager** lets a user manage cosmetic products: add them, view them in a list, open one to
see its details, edit it, and delete it. This milestone is **UI-only with fake / in-memory data** —
no database and no network yet (those arrive in later lectures). Data lives for the lifetime of the
process, which is exactly what the assignment asks for to demo CRUD.

- **Platform:** Android, **Kotlin**, **Jetpack Compose** (Material 3).
- **Architecture:** MVVM with **Unidirectional Data Flow (UDF)**.
- **Each CRUD operation has its own screen** (list · create · edit · detail), connected by
  Navigation-Compose.

---

## 2. The big picture — three layers, one direction

The app follows the three-layer architecture from the lectures (**UI → (Domain) → Data**), with
dependencies pointing strictly downward.

```
┌─────────────────────────────────────────────────────────────┐
│  UI LAYER  (ui/)                                              │
│                                                               │
│   Composables (screens + stateless components)                │
│        ▲  state flows DOWN            │  events flow UP        │
│        │  (immutable UiState)         ▼  (onClick, onChange…)  │
│   ViewModels  (ProductListViewModel, ProductEditorViewModel)  │
│        ▲ observes StateFlow           │ calls repo methods     │
└────────┼──────────────────────────────┼──────────────────────┘
         │                              ▼
┌────────┼───────────────────────────────────────────────────┐
│  DATA LAYER  (data/)                                         │
│   ProductRepository  ──  single source of truth              │
│   (InMemoryProductRepository holds a MutableStateFlow<List>)  │
└──────────────────────────────────────────────────────────────┘

          DOMAIN  (domain/):  CosmeticProduct, Category, ProductFilter
          — pure Kotlin, no Android, no framework imports.
```

**Why this shape (lecture: "Architecture — The Three Layers"):** the UI never owns the data; it
*observes* it and *sends events*. The data layer owns the truth. This is what makes the whole app
predictable and testable, and it is the backbone of every point below.

### Package map

| Package | Responsibility |
|---|---|
| `domain/` | Pure business model: `CosmeticProduct` (entity + expiry rules), `Category`, `ProductFilter` + the pure `filterProducts()` helper. No Android imports. |
| `data/` | `ProductRepository` interface, `InMemoryProductRepository` (single source of truth), `ServiceLocator` (shares one repo instance). |
| `ui/list/` | Read screen: `ProductListScreen`, `ProductListViewModel`, `ProductListUiState`, and stateless components (`ProductCard`, `ProductSearchBar`, `CategoryFilterRow`). |
| `ui/editor/` | Create **and** Edit screen (shared): `ProductEditorScreen`, `ProductEditorViewModel`, `ProductEditorUiState`. |
| `ui/detail/` | Read-only single-product screen: `ProductDetailScreen`. |
| `ui/common/` | Reusable **stateless** form controls: `CategoryDropdown`, `DatePickerField`, `ShelfLifePicker`, `DateFormatting`. |
| `ui/navigation/` | `CosmeticNavHost` — the four destinations and how they connect. |
| `ui/theme/` | Material 3 theme (grey palette). |
| `ui/ViewModelFactory.kt` | Builds ViewModels, injecting the shared repository (manual DI). |

---

## 3. The core mental model: `UI = f(state)` and UDF

Everything below is an instance of two lecture rules:

1. **`UI = f(state)`** — the screen is a pure function of an immutable state object. We never imperatively
   "set the text on a view"; we describe what the UI looks like for the current state, and Compose
   redraws when the state changes.
2. **Events up, state down** — components *send events upward* (`onClick`, `onQueryChange`) and
   *receive state downward*. The UI never mutates state directly.

Concretely, one full round trip looks like this (deleting a product):

```
User taps "Delete"  (event)
        ▼
ProductListScreen shows a confirmation dialog, then calls
viewModel.onDeleteProduct(id)                         ← event flows UP
        ▼
ProductListViewModel calls repository.delete(id)
        ▼
InMemoryProductRepository publishes a NEW list via its MutableStateFlow
        ▼
ProductListViewModel's combine(...) re-emits a NEW ProductListUiState  ← state flows DOWN
        ▼
collectAsStateWithLifecycle() in the screen receives it → LazyColumn recomposes
        ▼
Only the removed row disappears (stable keys) — no manual list rebuild
```

That last line is the heart of the "Excellent" rubric criteria — see §7.

---

## 4. State management — the single source of truth

### The data layer owns the list

`InMemoryProductRepository` holds the one authoritative copy of the product list:

```kotlin
private val _products = MutableStateFlow(seedProducts())          // writable — private
override val products: StateFlow<List<CosmeticProduct>> =         // read-only — public
    _products.asStateFlow()
```

- **`MutableStateFlow`** is the writable "box that always holds a current value and notifies observers
  when it changes." Only the repository can write to it.
- **`StateFlow`** is the read-only view exposed to the rest of the app.
- **Why both:** the lecture explicitly names "a public `MutableStateFlow`" as a bug — *"any caller
  can push arbitrary state, breaks single source of truth."* Exposing only `StateFlow` guarantees the
  **only** way to change the list is through `add` / `update` / `delete`. That is *single source of truth*.

### CRUD is functional and immutable

Each CRUD method reads the current immutable list and publishes a **new** list (never mutates in place):

```kotlin
fun add(product)    → _products.update { it + stored }                       // append
fun update(product) → _products.update { it.map { if (match) stamped else it } } // replace one
fun delete(id)      → _products.update { it.filterNot { it.id == id } }      // remove one
```

- Products are an immutable `data class`; we change one with `.copy(...)`, never by mutating fields
  (lecture: "data as an immutable structure").
- The repository assigns `id` / `lastModified` / `isSynced` itself, so the UI can't create duplicate
  ids or forget metadata.
- Emitting a **new list instance** is what triggers `StateFlow` to notify the UI → recomposition.

### Session state lives in the ViewModel, not in the composable

`ProductListViewModel` keeps the search query, the active filter, and the category filter as private
`MutableStateFlow`s and `combine`s them with the repository list into one derived `UiState`:

```kotlin
val uiState: StateFlow<ProductListUiState> =
    combine(repository.products, searchQuery, filter, categoryFilter) { products, q, f, cat ->
        ProductListUiState(visibleProducts = products.filterProducts(q, f, cat), …)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductListUiState())
```

**Why:** the lecture's "Phantom Rating Bug" is caused by keeping session state (like a search query)
in a local composable variable, where it is lost on rotation and can drift out of sync. By keeping it
in the ViewModel, the query/filter **survive rotation** and there is exactly one source of truth for
"what the list currently shows." `stateIn(... WhileSubscribed(5_000))` is the lecture's recommended
pattern for turning a cold `combine` flow into a hot, shared, lifecycle-aware state.

### The UiState pattern

Each screen has one immutable state class: `ProductListUiState` and `ProductEditorUiState`. They model
*everything the screen needs* in a single object (lecture "UiState pattern": one observable, no flag
soup, trivial to test). Derived facts are **computed properties** on the state (e.g.
`isCompletelyEmpty`, `submitLabel`), so the composable stays dumb and declarative.

---

## 5. How parents communicate with children (state hoisting)

Every reusable component in `ui/common/` and `ui/list/components/` is **stateless**: it receives its
value and a change-callback as parameters, and holds no business state of its own. Examples:

```kotlin
ProductSearchBar(query = …, onQueryChange = …)
CategoryDropdown(value = …, onValueChange = …)
ShelfLifePicker(value = …, onValueChange = …)
DatePickerField(value = …, onValueChange = …)
```

This is the lecture's **state hoisting** rule: the real state lives in the parent (ultimately the
ViewModel), and children only render it and emit events. The only state a child keeps is **ephemeral
UI state** that nobody else cares about — e.g. "is the dropdown menu open," "is the date dialog
showing," "is the overflow menu open" — kept locally with `remember`. That is precisely what the
lecture says ephemeral state is for.

**Why:** stateless components are reusable, independently previewable, and impossible to desync,
because they have no private source of truth to drift from.

---

## 6. Navigation — one activity, four screens

`CosmeticNavHost` defines four destinations with Navigation-Compose:

| Route | Screen | Purpose |
|---|---|---|
| `list` | `ProductListScreen` | Read: the list |
| `create` | `ProductEditorScreen(productId = null)` | Create |
| `edit/{productId}` | `ProductEditorScreen(productId = id)` | Update |
| `detail/{productId}` | `ProductDetailScreen(productId = id)` | Read one |

**Key choices (lecture: "Navigation — pass IDs, not objects"):**

- Navigation arguments are **product ids (Strings)**, never whole objects. The target screen looks the
  product up in the repository. This keeps a single source of truth, keeps the back stack small and
  serializable, and is the pattern the lecture insists on.
- Create and Edit are the **same screen** parameterised by a nullable id (`null` = create). This is a
  deliberate reuse, not a shortcut — it guarantees the two forms stay identical field-for-field.
- Each operation is therefore **"maintained in a separate activity/fragment"** (here: a separate
  composable destination), which the rubric requires for Create and Update.

---

## 7. How every grading criterion is satisfied (and where in the code)

> The rubric and the per-criterion "Excellent" bar are quoted in short; the mapping is to the exact
> files/mechanisms.

### Read — list (20%) → targeting **Excellent (4)**

> *"A recycler view is used, linked to a view model using a live data object. Or the activity/fragment
> is marshaling only the affected object/operation. No rebuild of the list/adapter or
> activity/fragment."*

- **`LazyColumn`** is the modern Compose equivalent of `RecyclerView` — it virtualizes rows (only
  visible items are composed). `ui/list/ProductListScreen.kt`.
- It is **linked to a ViewModel via observed state**: `viewModel.uiState.collectAsStateWithLifecycle()`.
  `StateFlow` + `collectAsStateWithLifecycle` is the modern, lifecycle-aware replacement for
  `LiveData` the rubric describes.
- **Only the affected item changes, no rebuild:** `items(..., key = { it.id })` gives every row a
  **stable identity**. When one product is added/edited/deleted, Compose diffs by key and recomposes
  **only that row** — the activity/fragment is never recreated and the list/adapter is never rebuilt
  by hand. This is exactly the "marshaling only the affected object" path.
- **`collectAsStateWithLifecycle`** (not plain `collectAsState`) is used deliberately — the lecture
  flags the plain version as a battery-drain pitfall; the lifecycle-aware one stops collecting in the
  background.

### Create (20%) → targeting **Excellent (4)**

> *"Only the created element is added. Separate activity/fragment. All main fields available. Labels
> for each input. Validation errors handled in this view."*

- **Separate screen:** `ui/editor/ProductEditorScreen.kt` on route `create` — its own destination,
  not a dialog and not part of the list screen.
- **Only the created element is added:** on save, `ProductEditorViewModel.onSubmit()` calls
  `repository.add(...)`, which appends one item; the list observes the new state and the single new row
  appears. The list is never rebuilt.
- **All main fields available:** name, description, category, shelf life, bought date, opened date,
  ingredients.
- **Label for each field:** every input has a visible `FieldLabel(...)` above it (e.g. `"Name *"`,
  `"Category *"`, `"Shelf life *"`).
- **Validation errors handled in this view:** `onSubmit()` validates name / category / shelf life and,
  on failure, writes per-field error strings into `ProductEditorUiState` (`nameError`, `categoryError`,
  `shelfLifeError`). The screen stays put and shows each error **under the offending field**; nothing
  is saved until the form is valid.

### Update (20%) → targeting **Excellent (4)**

> *"Only the updated element is passed back. Element properly identified. Separate activity/fragment.
> All main fields updatable. Labels for each field and existing values pre-populated. Validation errors
> handled in this view."*

- **Separate screen & shared form:** same `ProductEditorScreen`, entered on route `edit/{productId}`.
- **Properly identified:** the product id travels in the route; the ViewModel loads that exact product
  by id (`repository.getById(id)`).
- **Pre-populated:** in edit mode the ViewModel copies the existing product's fields into
  `ProductEditorUiState`, so every field shows its current value when the screen opens.
- **Only the updated element changes:** `onSubmit()` calls `repository.update(...)`, which replaces
  **only the matching item** by id (`map { if (id matches) new else old }`). The rest of the list is
  untouched, and the single changed row recomposes — the old entry is **not** left behind.
- **Correct button label:** the submit button reads **"Save"** in edit mode and **"Add"** in create
  mode (`ProductEditorUiState.submitLabel`). This directly avoids the rubric's named "Good-tier"
  mistake ("the update button label still says 'add'").
- **Labels + in-view validation:** identical to Create (same screen).
- **Domain rule:** the *opened date* is **write-once** — if it was already set, the field is rendered
  read-only. Enforced in both the field callback and `onSubmit()` (defense in depth), per the README.

### Delete (20%) → targeting **Excellent (4)**

> *"Only the id of the removed element is passed back. Element properly identified. A confirmation
> dialog is used."*

- **Confirmation dialog:** tapping Delete on a row sets `pendingDelete`, which shows a Material
  `AlertDialog` ("Delete product? … Cancel / Delete"). Nothing is removed until the user confirms.
  `ui/list/ProductListScreen.kt`.
- **Only the id is passed back:** on confirm, the screen calls `viewModel.onDeleteProduct(product.id)`
  — just the **id**, not the object — and the ViewModel calls `repository.delete(id)`.
- **Properly identified & removed cleanly:** the repository removes the matching item by id and
  publishes a new list; that one row disappears from the `LazyColumn`.

### GitHub / submission (20%)

- Only **source** is committed — the repo's `.gitignore` excludes `build/`, `.gradle/`, `.idea/`,
  `local.properties`, so no build directories are pushed.

---

## 8. The domain model, briefly

`CosmeticProduct` is an immutable `data class`. Stored fields live in the constructor `( )`; **derived**
facts are computed properties in the body `{ }` — they store nothing and are always consistent with
the stored data:

- `isOpened` — has an opened date been set?
- `expiryDate` — `openedDate + shelfLifeMonths` (null until opened).
- `isExpired`, `isAboutToExpire` — expiry status, used by the filter and the card's status line.

**Why computed, not stored:** storing `isExpired` as a field would create two sources of truth that
could drift apart (the "two sources of truth" bug). Computing it from the real fields keeps it correct
by construction. This is also the lecture's "push logic into pure functions" rule.

`ProductFilter` is an enum of the four Read views (All / Unopened / Opened / About to expire).
`filterProducts(query, filter, category)` is a **pure extension function** — same inputs, same output,
no Android — so it is unit-tested in milliseconds (`app/src/test/.../ProductFilterTest.kt`) with no
device. The `when` over the enum is exhaustive, so adding a fifth filter later won't compile until its
rule is handled — a built-in safety net.

---

## 9. Dependency injection — `ServiceLocator`

The lectures use **Hilt** for DI, but Hilt is introduced later in the course. For this UI-only
milestone we use a tiny manual container:

```kotlin
object ServiceLocator {
    val productRepository: ProductRepository by lazy { InMemoryProductRepository() }
}
```

`ViewModelFactory` reads this one shared repository and injects it into every ViewModel. **Why it
matters:** if each ViewModel created its own repository, the create screen and the list screen would
hold *different* lists and changes wouldn't propagate — a single-source-of-truth violation. One shared
instance guarantees a create/edit/delete on one screen is seen everywhere. This is the same
*architectural intent* as the example project's Hilt `@Singleton` repository, done by hand.

---

## 10. Why we avoided the lecture's named "AI bug" traps

The lectures catalogue specific pitfalls. This app was built to avoid each:

| Lecture pitfall | How we avoid it |
|---|---|
| State copied into a local `var` (Phantom Rating Bug) | Search/filter live in the ViewModel, not a composable `var`. |
| Public `MutableStateFlow` | All `MutableStateFlow`s are private; public surface is read-only `StateFlow`. |
| `Column { forEach }` instead of a lazy list | List uses `LazyColumn`. |
| Missing stable `key` on list items | `items(..., key = { it.id })`. |
| `SideEffect` writing state → recomposition loop | No such effect; the one `LaunchedEffect` (navigate-on-save) only reads `isSaved`. |
| Activity/Context reference held in a ViewModel | ViewModels hold only the repository; errors are surfaced as `UiState` fields. |
| Plain `collectAsState` (background battery drain) | Uses `collectAsStateWithLifecycle`. |
| Update button still says "Add" | `submitLabel` = "Save" in edit mode. |

---

## 11. What is intentionally *not* here yet

- **No database / no network.** Data is in-memory (`InMemoryProductRepository`). Persistence and REST
  sync belong to later lectures. The model already carries the fields those will need
  (`lastModified`, `isSynced`), and the list shows a small "waiting to sync" indicator as a
  forward-looking placeholder.
- The `ProductRepository` **interface** exists so that swapping the in-memory implementation for a
  Room/remote one later touches only the data layer — the UI is written against the interface.

---

## 12. File-by-file quick reference

| File | What it does |
|---|---|
| `domain/CosmeticProduct.kt` | The entity: stored fields + computed expiry rules. |
| `domain/Category.kt` | Category enum; each carries a label + a drawable icon. |
| `domain/ProductFilter.kt` | The four Read views + the pure `filterProducts()` helper. |
| `data/ProductRepository.kt` | Repository interface (the data-layer contract). |
| `data/InMemoryProductRepository.kt` | Single source of truth: `StateFlow<List>` + functional CRUD. |
| `data/ServiceLocator.kt` | Shares one repository instance across the app. |
| `ui/ViewModelFactory.kt` | Builds ViewModels, injecting the repository. |
| `ui/list/ProductListViewModel.kt` | Combines repo list + filters into `ProductListUiState`. |
| `ui/list/ProductListUiState.kt` | Immutable state for the list screen. |
| `ui/list/ProductListScreen.kt` | The Read list: `LazyColumn`, search, filters, FAB, delete dialog. |
| `ui/list/components/ProductCard.kt` | One stateless row; emits open/edit/delete events. |
| `ui/list/components/ProductSearchBar.kt` | Stateless search field. |
| `ui/list/components/CategoryFilterRow.kt` | Stateless category chip row. |
| `ui/editor/ProductEditorViewModel.kt` | Create/Edit logic: load, validate, save. |
| `ui/editor/ProductEditorUiState.kt` | Immutable form state + per-field errors. |
| `ui/editor/ProductEditorScreen.kt` | The shared Create/Edit form (labels + in-view validation). |
| `ui/detail/ProductDetailScreen.kt` | Read-only single-product view. |
| `ui/common/CategoryDropdown.kt` | Stateless category picker. |
| `ui/common/DatePickerField.kt` | Stateless labelled date field. |
| `ui/common/ShelfLifePicker.kt` | Stateless shelf-life picker (6/12/24 months + custom). |
| `ui/common/DateFormatting.kt` | Pure date/expiry formatting helpers. |
| `ui/navigation/CosmeticNavHost.kt` | The four destinations; passes ids, not objects. |
| `ui/theme/` | Material 3 grey theme. |
| `MainActivity.kt` | Single activity; hosts the theme + nav graph. |
