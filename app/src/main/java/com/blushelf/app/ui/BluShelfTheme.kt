package com.blushelf.app.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.blushelf.app.ColorPalette
import com.blushelf.app.ThemeMode

private val OceanLight = lightColorScheme(primary = Color(0xFF00639A), secondary = Color(0xFF4E616D), tertiary = Color(0xFF67587A), surface = Color(0xFFF8F9FF))
private val OceanDark = darkColorScheme(primary = Color(0xFF95CCFF), secondary = Color(0xFFB5C9D7), tertiary = Color(0xFFD2BFE7), surface = Color(0xFF101418))
private val IndigoLight = lightColorScheme(primary = Color(0xFF4555A5), secondary = Color(0xFF5B5D72), tertiary = Color(0xFF77536D))
private val IndigoDark = darkColorScheme(primary = Color(0xFFBBC3FF), secondary = Color(0xFFC4C5DD), tertiary = Color(0xFFE7BAD7))
private val TealLight = lightColorScheme(primary = Color(0xFF006A67), secondary = Color(0xFF4A6361), tertiary = Color(0xFF4B607C))
private val TealDark = darkColorScheme(primary = Color(0xFF4FDAD4), secondary = Color(0xFFB1CCCA), tertiary = Color(0xFFB3C8E8))

@Composable
fun BluShelfTheme(mode: ThemeMode, dynamic: Boolean, palette: ColorPalette, content: @Composable () -> Unit) {
    val dark = when (mode) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true }
    val context = LocalContext.current
    val colors = when {
        dynamic && Build.VERSION.SDK_INT >= 31 -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        palette == ColorPalette.INDIGO -> if (dark) IndigoDark else IndigoLight
        palette == ColorPalette.TEAL -> if (dark) TealDark else TealLight
        else -> if (dark) OceanDark else OceanLight
    }
    MaterialTheme(colorScheme = colors, content = content)
}
