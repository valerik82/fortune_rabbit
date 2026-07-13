package com.rabbitsluckandfortuneppamobs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Festive palette (spec §7): red, gold, cream and soft pastels.
val FortuneRed = Color(0xFFC62828)
val FortuneRedDark = Color(0xFF8E0000)
val FortuneGold = Color(0xFFF9A825)
val FortuneGoldLight = Color(0xFFFFD54F)
val FortuneCream = Color(0xFFFFF3E0)
val FortuneCreamDeep = Color(0xFFFFE0B2)
val SakuraPink = Color(0xFFF48FB1)
val BambooGreen = Color(0xFF66BB6A)
val InkBrown = Color(0xFF4E342E)

private val LightColors = lightColorScheme(
    primary = FortuneRed,
    onPrimary = Color.White,
    primaryContainer = FortuneRedDark,
    onPrimaryContainer = Color.White,
    secondary = FortuneGold,
    onSecondary = InkBrown,
    secondaryContainer = FortuneGoldLight,
    onSecondaryContainer = InkBrown,
    tertiary = SakuraPink,
    background = FortuneCream,
    onBackground = InkBrown,
    surface = Color.White,
    onSurface = InkBrown,
    surfaceVariant = FortuneCreamDeep,
    onSurfaceVariant = InkBrown,
)

private val DarkColors = darkColorScheme(
    primary = FortuneGold,
    onPrimary = InkBrown,
    primaryContainer = FortuneRedDark,
    onPrimaryContainer = Color.White,
    secondary = FortuneGoldLight,
    onSecondary = InkBrown,
    background = Color(0xFF2A1414),
    onBackground = FortuneCream,
    surface = Color(0xFF3A1E1E),
    onSurface = FortuneCream,
    surfaceVariant = Color(0xFF4A2A2A),
    onSurfaceVariant = FortuneCream,
)

@Composable
fun FortuneRabbitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
