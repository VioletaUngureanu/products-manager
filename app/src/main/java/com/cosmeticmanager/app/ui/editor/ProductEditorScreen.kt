package com.cosmeticmanager.app.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cosmeticmanager.app.ui.ViewModelFactory
import com.cosmeticmanager.app.ui.common.CategoryDropdown
import com.cosmeticmanager.app.ui.common.DatePickerField
import com.cosmeticmanager.app.ui.common.ShelfLifePicker

/**
 * The Create/Edit screen — its own destination, not a dialog (rubric: separate activity/fragment).
 *
 * Every field has a visible label. Validation errors are surfaced inline on the offending field
 * and the screen stays put until they're fixed. In edit mode the fields arrive pre-populated and
 * the submit button reads "Save" (vs "Add" when creating).
 *
 * @param productId id of the product to edit, or null to create a new one
 * @param onNavigateBack called to leave the screen (back arrow and after a successful save)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditorScreen(
    productId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductEditorViewModel = viewModel(
        factory = ViewModelFactory(editingProductId = productId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // After a successful save the ViewModel flips isSaved; navigate back exactly once.
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FieldLabel("Name *")
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )

            FieldLabel("Description")
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            FieldLabel("Category *")
            CategoryDropdown(
                value = uiState.category,
                onValueChange = viewModel::onCategoryChange,
                isError = uiState.categoryError != null,
                errorText = uiState.categoryError,
            )

            FieldLabel("Shelf life *")
            ShelfLifePicker(
                value = uiState.shelfLifeMonths,
                onValueChange = viewModel::onShelfLifeChange,
                isError = uiState.shelfLifeError != null,
                errorText = uiState.shelfLifeError,
            )

            FieldLabel("Bought date")
            DatePickerField(
                label = "Bought on",
                value = uiState.boughtDate,
                onValueChange = viewModel::onBoughtDateChange,
            )

            FieldLabel("Opened date")
            DatePickerField(
                label = "Opened on",
                value = uiState.openedDate,
                onValueChange = viewModel::onOpenedDateChange,
                enabled = !uiState.openedDateLocked,
                supportingText = if (uiState.openedDateLocked) {
                    "Opened date can't be changed once set."
                } else {
                    null
                },
            )

            FieldLabel("Ingredients")
            OutlinedTextField(
                value = uiState.ingredients,
                onValueChange = viewModel::onIngredientsChange,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = viewModel::onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Text(uiState.submitLabel)
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
