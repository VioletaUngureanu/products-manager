package com.cosmeticmanager.app.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The product categories the user can pick from.
 *
 * This is a closed set modelled as an enum. The user asked for a reasonable seed set that they can
 * edit later — add or remove entries here and the whole UI (the create/edit dropdown and the list
 * category chips) updates automatically because everything reads from [Category.entries].
 *
 * Each category carries a built-in Compose vector icon, used both on the list category chips and as
 * the leading glyph on each product row. These are stand-ins (any Material icon works) until the
 * real category PNGs are dropped into res/drawable and mapped here. We deliberately use only icons
 * from the always-bundled material-icons-core set, so no extra icon dependency is needed.
 */
enum class Category(
    val label: String,
    val icon: ImageVector,
) {
    HAIR("Hair", Icons.Filled.Person),
    BODY("Body", Icons.Filled.Favorite),
    FACE("Face", Icons.Filled.Face),
    SKINCARE("Skincare", Icons.Filled.Star),
    MAKEUP("Makeup", Icons.Filled.ThumbUp),
    FRAGRANCE("Fragrance", Icons.Filled.Place),
    OTHER("Other", Icons.Filled.Home),
}
