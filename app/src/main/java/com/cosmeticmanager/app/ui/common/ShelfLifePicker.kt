package com.cosmeticmanager.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions

private val PRESETS = listOf(6, 12, 24)

/**
 * Shelf-life selector matching the mockup: quick-pick chips for 6 / 12 / 24 months plus a
 * "Custom" chip that reveals a numeric field.
 *
 * Fully stateless w.r.t. the chosen value — [value] (months) and [onValueChange] are hoisted
 * (L03). Only "is the custom field showing?" is ephemeral local state.
 *
 * @param value the current shelf life in months, or null if nothing is chosen yet
 */
@Composable
fun ShelfLifePicker(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
) {
    // Custom mode is active when a value is set that isn't one of the presets.
    var customMode by remember { mutableStateOf(value != null && value !in PRESETS) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PRESETS.forEach { months ->
                FilterChip(
                    selected = !customMode && value == months,
                    onClick = {
                        customMode = false
                        onValueChange(months)
                    },
                    label = { Text("${months}M") },
                )
            }
            FilterChip(
                selected = customMode,
                onClick = {
                    customMode = true
                    // Clear any preset selection so the custom field starts empty unless already custom.
                    if (value in PRESETS) onValueChange(null)
                },
                label = { Text("Custom") },
            )
        }

        if (customMode) {
            OutlinedTextField(
                value = value?.toString() ?: "",
                onValueChange = { text ->
                    val digits = text.filter { it.isDigit() }.take(3)
                    onValueChange(digits.toIntOrNull())
                },
                label = { Text("Months") },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }

        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}
