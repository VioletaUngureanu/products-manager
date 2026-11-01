package com.cosmeticmanager.app.ui.editor

import com.cosmeticmanager.app.domain.Category
import java.time.LocalDate

/**
 * Immutable form state for the create/edit screen (L03 UiState pattern).
 *
 * Holds the raw editable field values plus per-field validation errors, so errors are shown in
 * place in this screen (rubric: "validation errors are handled in this view"). [isEditMode] drives
 * the title and the submit-button label ("Add" vs "Save").
 */
data class ProductEditorUiState(
    val isEditMode: Boolean = false,
    val isLoaded: Boolean = false,

    // Editable fields
    val name: String = "",
    val description: String = "",
    val category: Category? = null,
    val boughtDate: LocalDate? = null,
    val openedDate: LocalDate? = null,
    val shelfLifeMonths: Int? = null,
    val ingredients: String = "",

    // Whether the opened date was already set when editing began. If so, it is read-only
    // (Update rule: once an opened date is set it can't be changed).
    val openedDateLocked: Boolean = false,

    // Per-field validation errors (null = no error)
    val nameError: String? = null,
    val categoryError: String? = null,
    val shelfLifeError: String? = null,

    // Set to true once a save succeeds so the screen can navigate back.
    val isSaved: Boolean = false,
) {
    val hasErrors: Boolean
        get() = nameError != null || categoryError != null || shelfLifeError != null

    /** Label for the primary action button. */
    val submitLabel: String
        get() = if (isEditMode) "Save" else "Add"

    val screenTitle: String
        get() = if (isEditMode) "Edit product" else "Add a product"
}
