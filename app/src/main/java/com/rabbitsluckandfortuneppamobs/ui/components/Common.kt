package com.rabbitsluckandfortuneppamobs.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rabbitsluckandfortuneppamobs.R
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneCreamDeep
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGold
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGoldLight
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRed
import com.rabbitsluckandfortuneppamobs.ui.theme.InkBrown
import com.rabbitsluckandfortuneppamobs.ui.theme.SakuraPink
import kotlin.random.Random

// Gold trim + neon accent colors for the casino-floor visual style.
val GoldTrim = Color(0xFFFFD76A)
val NeonGold = Color(0xFFFFC72C)
val NeonPink = Color(0xFFFF4FA3)
val NeonPurple = Color(0xFF9B5CFF)

/** Adds a soft colored glow behind a shape — the neon-sign look used across buttons/badges. */
fun Modifier.neonGlow(color: Color, shape: Shape, elevation: androidx.compose.ui.unit.Dp = 14.dp): Modifier =
    this.shadow(elevation = elevation, shape = shape, ambientColor = color, spotColor = color)

/** Dark "casino floor" base gradient chosen by the equipped background item (spec §7). */
fun backgroundBrush(selectedBackground: String): Brush = when (selectedBackground) {
    "bg_garden" -> Brush.verticalGradient(
        listOf(Color(0xFF2B0A22), Color(0xFF4A0F35), Color(0xFF15050F))
    )
    "bg_night" -> Brush.verticalGradient(
        listOf(Color(0xFF130A2E), Color(0xFF1F1049), Color(0xFF090414))
    )
    // Default "festive" table felt — deep red-black casino floor.
    else -> Brush.verticalGradient(
        listOf(Color(0xFF2A0006), Color(0xFF4A0B10), Color(0xFF12000A))
    )
}

/** Centered neon glow overlay tinted per background so each theme reads as its own room. */
private fun glowBrush(selectedBackground: String): Brush {
    val tint = when (selectedBackground) {
        "bg_garden" -> NeonPink
        "bg_night" -> NeonPurple
        else -> NeonGold
    }
    return Brush.radialGradient(listOf(tint.copy(alpha = 0.28f), Color.Transparent))
}

/** Solid color used for the back of a face-down card (spec §5.8 card backs). */
fun cardBackColor(selectedCardBack: String): Color = when (selectedCardBack) {
    "back_gold" -> FortuneGold
    "back_lantern" -> Color(0xFF7B1FA2)
    else -> FortuneRed
}

fun cardBackGlyph(selectedCardBack: String): String = when (selectedCardBack) {
    "back_gold" -> "🀄"
    "back_lantern" -> "🏮"
    else -> "🎴"
}

/** Full-screen themed container: dark gradient + neon glow + a scattering of glitter. */
@Composable
fun FortuneBackground(
    selectedBackground: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush(selectedBackground))
    ) {
        Box(modifier = Modifier.fillMaxSize().background(glowBrush(selectedBackground)))
        Sparkles()
        content()
    }
}

/** A handful of fixed, low-alpha glints for a casino-floor sparkle without any animation cost. */
@Composable
private fun Sparkles() {
    val dots = remember {
        val rng = Random(42)
        List(18) {
            Triple(rng.nextFloat(), rng.nextFloat(), 1.5f + rng.nextFloat() * 2.5f)
        }
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        dots.forEach { (fx, fy, radius) ->
            drawCircle(
                color = GoldTrim.copy(alpha = 0.35f),
                radius = radius.dp.toPx(),
                center = Offset(fx * size.width, fy * size.height)
            )
        }
    }
}

/** Coin balance chip shown on the HUD (spec §5.4, §5.2) with a neon-gold glow. */
@Composable
fun CoinBadge(coins: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .neonGlow(NeonGold.copy(alpha = 0.6f), RoundedCornerShape(50), elevation = 10.dp)
            .clip(RoundedCornerShape(50))
            .border(1.5.dp, GoldTrim, RoundedCornerShape(50)),
        shape = RoundedCornerShape(50),
        color = FortuneGoldLight,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(GoldTrim, FortuneGold)))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🪙", fontSize = 18.sp)
            Text(
                "  $coins",
                color = InkBrown,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/** Big primary menu button — gold-trimmed with a soft neon glow (spec §5.2). */
@Composable
fun MenuButton(
    label: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    container: Color = FortuneRed,
    contentColor: Color = Color.White
) {
    val shape = RoundedCornerShape(18.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .neonGlow(GoldTrim.copy(alpha = 0.45f), shape, elevation = 8.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = contentColor
        ),
        border = BorderStroke(1.5.dp, GoldTrim), // gold trim for the premium look
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text("$emoji  $label", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/** Row of up to three stars (spec §3.4). */
@Composable
fun StarRow(stars: Int, max: Int = 3, starSize: Int = 22, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        for (i in 1..max) {
            Text(
                text = if (i <= stars) "⭐" else "☆",
                fontSize = starSize.sp,
                color = if (i <= stars) FortuneGold else InkBrown
            )
        }
    }
}

/** Circular header badge with a gold ring + neon glow, showing an emoji glyph. */
@Composable
fun EmblemCircle(glyph: String, size: Int = 96, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .size(size.dp)
            .neonGlow(NeonGold.copy(alpha = 0.5f), CircleShape, elevation = 12.dp)
            .border(3.dp, Brush.verticalGradient(listOf(GoldTrim, FortuneGold)), CircleShape),
        shape = CircleShape,
        color = FortuneCreamDeep,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(glyph, fontSize = (size * 0.5f).sp, textAlign = TextAlign.Center)
        }
    }
}

/**
 * The custom casino-style "fortune rabbit" mascot in a gold-ringed, glowing medallion.
 * Used on the splash and main menu for the default rabbit skin.
 */
@Composable
fun RabbitEmblem(size: Int = 110, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .size(size.dp)
            .neonGlow(NeonGold.copy(alpha = 0.55f), CircleShape, elevation = 16.dp)
            .border(4.dp, Brush.verticalGradient(listOf(GoldTrim, FortuneGold)), CircleShape),
        shape = CircleShape,
        color = FortuneCreamDeep,
        shadowElevation = 10.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.rabbit_mascot),
                contentDescription = "Rabbit of Fortune mascot",
                modifier = Modifier.size((size * 0.82f).dp)
            )
        }
    }
}

/** Screen title styled consistently across screens, with an optional neon glow. */
@Composable
fun ScreenTitle(
    text: String,
    color: Color = Color.White,
    glow: Color = NeonGold,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineMedium.copy(
            shadow = Shadow(color = glow.copy(alpha = 0.7f), blurRadius = 18f)
        )
    )
}

/**
 * A poker-chip-styled face-down card back: a dashed rim over a solid disc, echoing
 * casino chip edge notches, with the card-back glyph centered on top.
 */
@Composable
fun PokerChipBack(selectedCardBack: String, modifier: Modifier = Modifier) {
    val base = cardBackColor(selectedCardBack)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val stroke = 3.dp.toPx()
            val outerRadius = size.minDimension / 2f - stroke
            drawCircle(
                color = base,
                radius = outerRadius
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = outerRadius - stroke,
                style = Stroke(
                    width = stroke,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(outerRadius / 4.5f, outerRadius / 8f), 0f
                    )
                )
            )
            drawCircle(
                color = GoldTrim,
                radius = outerRadius * 0.42f,
                style = Stroke(width = stroke * 0.8f)
            )
        }
        Text(cardBackGlyph(selectedCardBack), fontSize = 26.sp, color = Color.White)
    }
}
