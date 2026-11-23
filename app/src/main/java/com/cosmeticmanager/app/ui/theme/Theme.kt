package com.cosmeticmanager.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Light grey scheme. Only the roles the app actually uses are overridden:
 *  - primary        -> buttons / FAB / selected chips  (#747474)
 *  - surfaceVariant -> card backgrounds                (#A8A8A8)
 * plus the matching on-colors so text stays readable, and surface/background for the rest.
 */
private val LightColorScheme = lightColorScheme(
    primary = ButtonGrey,
    onPrimary = OnDark,
    secondary = ButtonGreyDark,
    onSecondary = OnDark,
    tertiary = AccentGrey,
    onTertiary = OnDark,
    background = Background,
    onBackground = OnLight,
    surface = Surface,
    onSurface = OnLight,
    surfaceVariant = CardGrey,
    onSurfaceVariant = OnCard,
)

private val DarkColorScheme = darkColorScheme(
    primary = ButtonGreyLight,
    onPrimary = OnLight,
    secondary = ButtonGrey,
    onSecondary = OnLight,
    tertiary = AccentGreyLight,
    onTertiary = OnLight,
    background = BackgroundDark,
    onBackground = OnDark,
    surface = SurfaceDark,
    onSurface = OnDark,
    surfaceVariant = CardGreyDark,
    onSurfaceVariant = OnDark,
)

@Composable
fun CosmeticManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Dynamic color is deliberately NOT used: on Android 12+ it would replace this grey palette
    // with wallpaper-derived colors, so we always use our own scheme.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
