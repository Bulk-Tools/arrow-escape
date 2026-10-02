package com.bulktools.arrowescape.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class ThemeDef(
    val name: String,
    val bgTop: Color,
    val bgBottom: Color,
    val tile: Color,
    val arrow: Color,
    val accent: Color,
    val gold: Color
)

object AppThemes {
    val all: List<ThemeDef> = listOf(
        ThemeDef(
            name = "Midnight",
            bgTop = Color(0xFF0B1020),
            bgBottom = Color(0xFF16213E),
            tile = Color(0xFF1F2A44),
            arrow = Color(0xFFF5F7FA),
            accent = Color(0xFF4CC9F0),
            gold = Color(0xFFFFD166)
        ),
        ThemeDef(
            name = "Sunset",
            bgTop = Color(0xFF2B1A3D),
            bgBottom = Color(0xFF7B2D5B),
            tile = Color(0xFF4A2B5C),
            arrow = Color(0xFFFFF3E0),
            accent = Color(0xFFFF9E5E),
            gold = Color(0xFFFFD166)
        ),
        ThemeDef(
            name = "Forest",
            bgTop = Color(0xFF0D2818),
            bgBottom = Color(0xFF1B4D2E),
            tile = Color(0xFF234D33),
            arrow = Color(0xFFF1FAEE),
            accent = Color(0xFF80ED99),
            gold = Color(0xFFFFD166)
        ),
        ThemeDef(
            name = "Candy",
            bgTop = Color(0xFF2D1B4E),
            bgBottom = Color(0xFF6A1B9A),
            tile = Color(0xFF4A2B7A),
            arrow = Color(0xFFFFFFFF),
            accent = Color(0xFFFF6EC7),
            gold = Color(0xFFFFE66D)
        )
    )
}

val LocalThemeDef = compositionLocalOf { AppThemes.all[0] }

@Composable
fun GameTheme(themeIndex: Int, content: @Composable () -> Unit) {
    val def = AppThemes.all[themeIndex.coerceIn(AppThemes.all.indices)]
    val colorScheme = darkColorScheme(
        primary = def.accent,
        onPrimary = Color.White,
        surface = def.tile,
        onSurface = Color.White,
        background = def.bgBottom,
        onBackground = Color.White,
        secondary = def.gold,
        onSecondary = Color.Black,
        error = Color(0xFFFF6B6B),
        onError = Color.White
    )
    CompositionLocalProvider(LocalThemeDef provides def) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}
