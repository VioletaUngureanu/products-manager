package com.cosmeticmanager.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Neutral grey palette, built around the two colors from the original design:
 *  - cards:   #A8A8A8  (-> [CardGrey], mapped to surfaceVariant)
 *  - buttons: #747474  (-> [ButtonGrey], mapped to primary)
 *
 * The rest are tones chosen to sit in harmony with those two: a darker pressed/variant grey,
 * light near-white surfaces, and readable on-colors. No hue — a clean monochrome scheme.
 */

// --- Light theme greys ---
val ButtonGrey = Color(0xFF747474)        // primary (buttons, FAB, selected chips)
val ButtonGreyDark = Color(0xFF5C5C5C)    // secondary / pressed-darker tone
val AccentGrey = Color(0xFF8C8C8C)        // tertiary (a mid tone between the two anchors)

val CardGrey = Color(0xFFA8A8A8)          // surfaceVariant (card backgrounds)
val Surface = Color(0xFFF5F5F5)           // app surface (near-white)
val Background = Color(0xFFFAFAFA)        // window background (lightest)

val OnDark = Color(0xFFFFFFFF)            // text/icons on the dark greys (buttons) -> white
val OnLight = Color(0xFF1C1C1C)           // text/icons on light surfaces -> near-black
val OnCard = Color(0xFF1F1F1F)            // text/icons on the card grey -> near-black (readable on #A8A8A8)

// --- Dark theme greys (lighter tones so they read on a dark background) ---
val ButtonGreyLight = Color(0xFFBDBDBD)   // primary in dark mode
val AccentGreyLight = Color(0xFFCFCFCF)   // tertiary in dark mode
val CardGreyDark = Color(0xFF4A4A4A)      // surfaceVariant (cards) in dark mode
val SurfaceDark = Color(0xFF1E1E1E)       // surface in dark mode
val BackgroundDark = Color(0xFF141414)    // background in dark mode
