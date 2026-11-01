package com.cosmeticmanager.app.domain

import androidx.annotation.DrawableRes
import com.cosmeticmanager.app.R

/**
 * The product categories the user can pick from.
 *
 * This is a closed set modelled as an enum. The user asked for a reasonable seed set that they can
 * edit later — add or remove entries here and the whole UI (the create/edit dropdown and the list
 * category chips) updates automatically because everything reads from [Category.entries].
 *
 * Each category carries a drawable resource id ([iconRes]) for its PNG icon in res/drawable. The UI
 * resolves it with `painterResource(iconRes)`. To change an icon, drop a new PNG into res/drawable
 * and update the reference here — nothing else needs to change.
 */
enum class Category(
    val label: String,
    @DrawableRes val iconRes: Int,
) {
    HAIR("Hair", R.drawable.hair),
    BODY("Body", R.drawable.cream),
    FACE("Face", R.drawable.facecream),
    SKINCARE("Skincare", R.drawable.skincare),
    MAKEUP("Makeup", R.drawable.cosmetics),
    FRAGRANCE("Fragrance", R.drawable.parfum),
    OTHER("Other", R.drawable.other),
}
