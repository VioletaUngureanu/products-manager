package com.cosmeticmanager.app.ui.common

import com.cosmeticmanager.app.domain.CosmeticProduct
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

/** Format a date for display, or a dash when absent. */
fun LocalDate?.formatOrDash(): String = this?.format(displayFormatter) ?: "—"

/**
 * A short, human-friendly expiry status line for a product, matching the mockup's wording:
 *  - unopened  -> "Shelf life: N months"
 *  - expired   -> "Expired N days ago"
 *  - opened    -> "Expires in N days / months"
 */
fun CosmeticProduct.expiryStatusText(): String {
    val days = daysUntilExpiry ?: return "Shelf life: $shelfLifeMonths months"
    return when {
        days < 0 -> "Expired ${humanDuration(abs(days))} ago"
        days == 0L -> "Expires today"
        else -> "Expires in ${humanDuration(days)}"
    }
}

/** Turn a day count into "N days" / "N months" (rounded) for compact display. */
private fun humanDuration(days: Long): String = when {
    days < 45 -> "$days day${if (days == 1L) "" else "s"}"
    else -> {
        val months = (days / 30.0).toLong().coerceAtLeast(1)
        "$months month${if (months == 1L) "" else "s"}"
    }
}
