package com.cosmeticmanager.app.ui.list.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cosmeticmanager.app.domain.Category

/**
 * A horizontally-scrolling row of category filter chips (the "Categories" strip in the mockup).
 *
 * Stateless: the currently-[selected] category and the [onCategoryClick] callback are hoisted.
 * Tapping the selected chip again clears the filter (handled by the ViewModel's toggle logic).
 */
@Composable
fun CategoryFilterRow(
    selected: Category?,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(Category.entries.toList(), key = { it.name }) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onCategoryClick(category) },
                label = { Text(category.label) },
                leadingIcon = {
                    Image(
                        painter = painterResource(category.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        }
    }
}
