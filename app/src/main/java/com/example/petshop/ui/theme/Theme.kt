package com.example.petshop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = IndigoPrimary,
    onPrimary          = Color.White,
    primaryContainer   = SoftLavender,
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary          = EmeraldAccent,
    onSecondary        = Color.White,
    secondaryContainer = SoftMint,
    onSecondaryContainer = Color(0xFF052E2B),
    tertiary           = SkyAccent,
    onTertiary         = Color.White,
    tertiaryContainer  = SoftSky,
    onTertiaryContainer = Color(0xFF082F49),
    error              = Color(0xFFBA1A1A),
    onError            = Color.White,
    errorContainer     = SoftRose,
    onErrorContainer   = Color(0xFF5F1014),
    background         = SurfaceLight,
    surface            = SurfaceCardLight,
    surfaceVariant     = Color(0xFFE9EEF7),
    onBackground       = OnSurfaceLight,
    onSurface          = OnSurfaceLight,
    onSurfaceVariant   = OnSurfaceMutedLight,
    outline            = Color(0xFFD0D7E2),
)

private val DarkColorScheme = darkColorScheme(
    primary            = IndigoDarkPrimary,
    onPrimary          = Color(0xFF24195D),
    primaryContainer   = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE9E7FF),
    secondary          = Color(0xFF6EE7B7),
    onSecondary        = Color(0xFF052E2B),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary           = Color(0xFF7DD3FC),
    onTertiary         = Color(0xFF082F49),
    tertiaryContainer  = Color(0xFF0C4A6E),
    onTertiaryContainer = Color(0xFFE0F2FE),
    background         = IndigoDarkSurface,
    surface            = SurfaceCardDark,
    surfaceVariant     = IndigoDarkSurfaceAlt,
    onBackground       = OnSurfaceDark,
    onSurface          = OnSurfaceDark,
    onSurfaceVariant   = OnSurfaceMutedDark,
    outline            = Color(0xFF334155),
)

@Composable
fun PetShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = AppShapes,
        content     = content
    )
}