package com.cosmeticmanager.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cosmeticmanager.app.domain.Category

/**
 * A labelled dropdown that lets the user choose one [Category].
 *
 * Stateless w.r.t. selection — the chosen [value] and [onValueChange] are hoisted (L03); only the
 * expanded/collapsed flag is local ephemeral state. Options come straight from [Category.entries],
 * so editing the enum updates this menu automatically.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    value: Category?,
    onValueChange: (Category) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value?.label ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Choose a category") },
            leadingIcon = value?.let {
                {
                    Image(
                        painter = painterResource(it.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = isError,
            supportingText = errorText?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Category.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.label) },
                    leadingIcon = {
                        Image(
                            painter = painterResource(category.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    onClick = {
                        onValueChange(category)
                        expanded = false
                    },
                )
            }
        }
    }
}
