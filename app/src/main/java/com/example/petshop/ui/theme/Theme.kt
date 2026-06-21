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
    primary            = Teal800,
    onPrimary          = Color.White,
    primaryContainer   = Teal100,
    onPrimaryContainer = Color(0xFF00201B),
    secondary          = Color(0xFF4A635E),
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFCCE8E3),
    onSecondaryContainer = Color(0xFF051F1B),
    tertiary           = Color(0xFF456179),
    onTertiary         = Color.White,
    tertiaryContainer  = Color(0xFFCCE5FF),
    onTertiaryContainer = Color(0xFF001D31),
    error              = Color(0xFFBA1A1A),
    onError            = Color.White,
    background         = SurfaceLight,
    surface            = SurfaceLight,
    onBackground       = OnSurfaceLight,
    onSurface          = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary            = TealDark200,
    onPrimary          = TealDark800,
    primaryContainer   = Teal600,
    onPrimaryContainer = Teal050,
    secondary          = Color(0xFFB0CCC7),
    onSecondary        = Color(0xFF1C3531),
    secondaryContainer = Color(0xFF334B47),
    onSecondaryContainer = Color(0xFFCCE8E3),
    background         = SurfaceDark,
    surface            = SurfaceDark,
    onBackground       = OnSurfaceDark,
    onSurface          = OnSurfaceDark,
)

@Composable
fun PetShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,          // disabled so teal theme always shows
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
        content     = content
    )
}