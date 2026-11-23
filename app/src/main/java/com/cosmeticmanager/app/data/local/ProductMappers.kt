package com.cosmeticmanager.app.data.local

import com.cosmeticmanager.app.domain.CosmeticProduct

/**
 * Converts between the storage type [ProductEntity] and the domain type [CosmeticProduct].
 *
 * The UI layer only ever sees [CosmeticProduct]; the database layer only ever sees [ProductEntity].
 * These two functions are the single bridge, so neither side leaks into the other.
 */

/** Database row -> domain model (read path). */
fun ProductEntity.toDomain(): CosmeticProduct =
    CosmeticProduct(
        id = id,
        lastModified = lastModified,
        isSynced = isSynced,
        name = name,
        description = description,
        category = category,
        boughtDate = boughtDate,
        openedDate = openedDate,
        shelfLifeMonths = shelfLifeMonths,
        ingredients = ingredients,
    )

/** Domain model -> database row (write path). A domain id of 0 tells Room to generate a new id. */
fun CosmeticProduct.toEntity(): ProductEntity =
    ProductEntity(
        id = id,
        name = name,
        description = description,
        category = category,
        boughtDate = boughtDate,
        openedDate = openedDate,
        shelfLifeMonths = shelfLifeMonths,
        ingredients = ingredients,
        lastModified = lastModified,
        isSynced = isSynced,
    )
