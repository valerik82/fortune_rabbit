package com.rabbitsluckandfortuneppamobs.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rabbitsluckandfortuneppamobs.models.GameMode
import com.rabbitsluckandfortuneppamobs.models.LevelResult
import com.rabbitsluckandfortuneppamobs.ui.components.MenuButton
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGold
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRed
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRedDark
import com.rabbitsluckandfortuneppamobs.ui.theme.InkBrown

/** Semi-transparent scrim wrapping a centered dialog card. */
@Composable
private fun OverlayScrim(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun DialogCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        modifier = Modifier
            .padding(28.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/** Pause menu (spec §5.5). */
@Composable
fun PauseDialog(onResume: () -> Unit, onRestart: () -> Unit, onMenu: () -> Unit) {
    OverlayScrim {
        DialogCard {
            Text("⏸ Paused", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = FortuneRed)
            Spacer(Modifier.height(20.dp))
            MenuButton("Resume", "▶️", onResume, container = FortuneRed)
            MenuButton("Restart", "🔄", onRestart, container = FortuneGold, contentColor = InkBrown)
            MenuButton("Main Menu", "🏠", onMenu, container = FortuneRedDark)
        }
    }
}

/** Level complete summary (spec §5.6, §8 star + rabbit celebration animation). */
@Composable
fun LevelCompleteOverlay(
    result: LevelResult,
    mode: GameMode,
    hasNextLevel: Boolean,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onMenu: () -> Unit
) {
    OverlayScrim {
        DialogCard {
            // Rabbit celebration bounce.
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "celebrate"
            )
            Text(
                "🐰",
                fontSize = 56.sp,
                modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
            )
            Text(
                if (mode == GameMode.DAILY_CHALLENGE) "Daily Challenge Complete!"
                else "Level Complete!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = FortuneRed,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            AnimatedStars(result.stars)
            Spacer(Modifier.height(16.dp))

            StatLine("Score", "${result.score}")
            StatLine("Time", formatTime(result.timeSeconds))
            StatLine("Moves", "${result.moves}")
            if (mode == GameMode.DAILY_CHALLENGE && result.streak > 0) {
                StatLine("Daily Streak", "🔥 ${result.streak}")
            }
            Spacer(Modifier.height(8.dp))
            CoinReward(result.coinsEarned)
            Spacer(Modifier.height(16.dp))

            if (mode == GameMode.CLASSIC && hasNextLevel) {
                MenuButton("Next Level", "⏭️", onNext, container = FortuneRed)
            }
            MenuButton("Replay", "🔄", onReplay, container = FortuneGold, contentColor = InkBrown)
            MenuButton("Main Menu", "🏠", onMenu, container = FortuneRedDark)
        }
    }
}

/** Timed-mode failure (spec §4.3). */
@Composable
fun TimeUpOverlay(onRetry: () -> Unit, onMenu: () -> Unit) {
    OverlayScrim {
        DialogCard {
            Text("⏰", fontSize = 52.sp)
            Text("Time's Up!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = FortuneRed)
            Text(
                "Try to clear the board faster next time.",
                fontSize = 14.sp,
                color = InkBrown,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            MenuButton("Try Again", "🔄", onRetry, container = FortuneRed)
            MenuButton("Main Menu", "🏠", onMenu, container = FortuneRedDark)
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, color = InkBrown)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkBrown)
    }
}

/** Coins fly in one at a time (spec §8 coin reward animation). */
@Composable
private fun CoinReward(coins: Int) {
    Surface(
        shape = RoundedCornerShape(50),
        color = FortuneGold,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🪙", fontSize = 22.sp)
            Text(
                "  +$coins coins",
                color = InkBrown,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Stars appear one by one with a small pop (spec §8 star appearing animation). */
@Composable
private fun AnimatedStars(stars: Int) {
    Row {
        for (i in 1..3) {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(stars) {
                kotlinx.coroutines.delay(200L * i)
                visible = i <= stars
            }
            val scale by animateFloatAsState(
                targetValue = if (visible) 1f else 0.4f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "star$i"
            )
            Text(
                text = if (i <= stars) "⭐" else "☆",
                fontSize = 40.sp,
                color = if (i <= stars) FortuneGold else Color(0xFFDDDDDD),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .graphicsLayer {
                        val s = if (i <= stars) scale else 1f
                        scaleX = s; scaleY = s
                    }
            )
        }
    }
}

@Composable
private fun SpacerW(width: Int) { Spacer(Modifier.width(width.dp)) }
