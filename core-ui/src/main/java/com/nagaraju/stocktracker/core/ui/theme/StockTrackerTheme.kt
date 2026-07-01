package com.nagaraju.stocktracker.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary    = Blue80,
    secondary  = GainGreen80,
    error      = LossRed80,
    background = SurfaceDark,
    surface    = SurfaceDark,
    onPrimary  = Neutral10,
    onBackground = Neutral90,
    onSurface  = Neutral90,
)

private val LightColorScheme = lightColorScheme(
    primary    = Blue40,
    secondary  = GainGreen40,
    error      = LossRed40,
    background = SurfaceLight,
    surface    = SurfaceLight,
    onPrimary  = Neutral99,
    onBackground = Neutral20,
    onSurface  = Neutral20,
)

/**
 * Root theme composable applied once in [MainActivity]. Every screen across
 * every feature module composes inside this theme, so color, typography,
 * and shape stay consistent without each feature redeclaring them.
 *
 * @param darkTheme    Defaults to the system setting. There is currently no
 *                      in-app override toggle — the app simply follows the
 *                      device's light/dark preference automatically.
 * @param dynamicColor Uses Android 12+ wallpaper-derived color when available,
 *                      falling back to the static brand palette on older
 *                      devices — a deliberate, low-cost personalization touch.
 */
@Composable
fun StockTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = StockTrackerTypography,
        shapes      = StockTrackerShapes,
        content     = content,
    )
}
