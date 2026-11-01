package com.cosmeticmanager.app.ui.editor

import androidx.lifecycle.ViewModel
import com.cosmeticmanager.app.data.ProductRepository
import com.cosmeticmanager.app.domain.Category
import com.cosmeticmanager.app.domain.CosmeticProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/**
 * State holder for the shared create/edit form (L03 ViewModel + StateFlow).
 *
 * One ViewModel serves both Create and Edit: when [editingProductId] is null we're creating; when
 * it points at an existing product we load it and pre-populate every field (rubric: "existing
 * values are pre-populated"). All form state lives here so it survives rotation.
 */
class ProductEditorViewModel(
    private val repository: ProductRepository,
    private val editingProductId: String?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductEditorUiState())
    val uiState: StateFlow<ProductEditorUiState> = _uiState.asStateFlow()

    init {
        if (editingProductId != null) {
            val existing = repository.getById(editingProductId)
            if (existing != null) {
                _uiState.value = ProductEditorUiState(
                    isEditMode = true,
                    isLoaded = true,
                    name = existing.name,
                    description = existing.description,
                    category = existing.category,
                    boughtDate = existing.boughtDate,
                    openedDate = existing.openedDate,
                    shelfLifeMonths = existing.shelfLifeMonths,
                    ingredients = existing.ingredients,
                    openedDateLocked = existing.openedDate != null,
                )
            } else {
                // The product was deleted meanwhile; fall back to create mode so we don't crash.
                _uiState.value = ProductEditorUiState(isLoaded = true)
            }
        } else {
            _uiState.update { it.copy(isLoaded = true) }
        }
    }

    // --- field change events (clear that field's error as the user fixes it) ---

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null) }

    fun onDescriptionChange(value: String) =
        _uiState.update { it.copy(description = value) }

    fun onCategoryChange(value: Category) =
        _uiState.update { it.copy(category = value, categoryError = null) }

    fun onBoughtDateChange(value: LocalDate?) =
        _uiState.update { it.copy(boughtDate = value) }

    /**
     * Set the opened date. Honoured only when it was not already locked — the write-once rule
     * (Update: "the opened date can't be changed once set") applies here too.
     */
    fun onOpenedDateChange(value: LocalDate?) = _uiState.update {
        if (it.openedDateLocked) it else it.copy(openedDate = value)
    }

    fun onShelfLifeChange(months: Int?) =
        _uiState.update { it.copy(shelfLifeMonths = months, shelfLifeError = null) }

    fun onIngredientsChange(value: String) =
        _uiState.update { it.copy(ingredients = value) }

    /**
     * Validate and save. On validation failure the errors are written back into the state and the
     * screen stays put (errors handled in place). On success the repository is updated and
     * [ProductEditorUiState.isSaved] flips true to let the screen navigate back.
     */
    fun onSubmit() {
        val state = _uiState.value

        val nameError = if (state.name.isBlank()) "Name is required" else null
        val categoryError = if (state.category == null) "Please choose a category" else null
        val shelfLifeError = when {
            state.shelfLifeMonths == null -> "Shelf life is required"
            state.shelfLifeMonths <= 0 -> "Shelf life must be at least 1 month"
            else -> null
        }

        if (nameError != null || categoryError != null || shelfLifeError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    categoryError = categoryError,
                    shelfLifeError = shelfLifeError,
                )
            }
            return
        }

        // Safe to unwrap: validation above guarantees these are non-null.
        val category = state.category!!
        val shelfLife = state.shelfLifeMonths!!
        val name = state.name.trim()
        val description = state.description.trim()
        val ingredients = state.ingredients.trim()

        if (state.isEditMode && editingProductId != null) {
            val existing = repository.getById(editingProductId)
            if (existing != null) {
                repository.update(
                    existing.copy(
                        name = name,
                        description = description,
                        category = category,
                        boughtDate = state.boughtDate,
                        // Preserve a previously-locked opened date; otherwise allow setting it once.
                        openedDate = if (existing.openedDate != null) existing.openedDate else state.openedDate,
                        shelfLifeMonths = shelfLife,
                        ingredients = ingredients,
                    ),
                )
            }
        } else {
            repository.add(
                CosmeticProduct(
                    id = "", // assigned by the repository
                    name = name,
                    description = description,
                    category = category,
                    boughtDate = state.boughtDate,
                    openedDate = state.openedDate,
                    shelfLifeMonths = shelfLife,
                    ingredients = ingredients,
                ),
            )
        }

        _uiState.update { it.copy(isSaved = true) }
    }
}
