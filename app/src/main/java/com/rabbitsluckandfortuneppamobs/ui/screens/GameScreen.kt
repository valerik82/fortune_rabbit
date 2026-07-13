package com.rabbitsluckandfortuneppamobs.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rabbitsluckandfortuneppamobs.models.Card
import com.rabbitsluckandfortuneppamobs.models.GameMode
import com.rabbitsluckandfortuneppamobs.ui.GameUiState
import com.rabbitsluckandfortuneppamobs.ui.components.CoinBadge
import com.rabbitsluckandfortuneppamobs.ui.components.FortuneBackground
import com.rabbitsluckandfortuneppamobs.ui.components.GoldTrim
import com.rabbitsluckandfortuneppamobs.ui.components.PokerChipBack
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGold
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGoldLight
import com.rabbitsluckandfortuneppamobs.ui.theme.InkBrown
import kotlin.math.abs

/** The main game screen (spec §5.4). */
@Composable
fun GameScreen(
    state: GameUiState,
    selectedCardBack: String,
    selectedBackground: String,
    hasNextLevel: Boolean,
    onCardTapped: (Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onNextLevel: () -> Unit,
    onMenu: () -> Unit
) {
    FortuneBackground(selectedBackground = selectedBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameHud(state = state, onPause = onPause)

            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(state.difficulty.columns),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                ) {
                    items(state.cards, key = { it.id }) { card ->
                        MemoryCard(
                            card = card,
                            cardBack = selectedCardBack,
                            enabled = !state.inputLocked && !state.isPaused,
                            onClick = { onCardTapped(card.id) }
                        )
                    }
                }
            }
        }

        if (state.isPaused && !state.isComplete && !state.isFailed) {
            PauseDialog(onResume = onResume, onRestart = onRestart, onMenu = onMenu)
        }

        state.result?.let { result ->
            LevelCompleteOverlay(
                result = result,
                mode = state.mode,
                hasNextLevel = hasNextLevel,
                onNext = onNextLevel,
                onReplay = onRestart,
                onMenu = onMenu
            )
        }

        if (state.isFailed) {
            TimeUpOverlay(onRetry = onRestart, onMenu = onMenu)
        }
    }
}

@Composable
private fun GameHud(state: GameUiState, onPause: () -> Unit) {
    Surface(color = Color.Black.copy(alpha = 0.18f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                HudStat(label = "Moves", value = "${state.moves}")
                Text(
                    state.difficulty.displayName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val timeText = if (state.timeLimit > 0) formatTime(state.timeLeft)
                else formatTime(state.elapsedSeconds)
                HudStat(
                    label = if (state.timeLimit > 0) "Time Left" else "Time",
                    value = timeText,
                    valueColor = if (state.timeLimit in 1..10) FortuneGoldLight else Color.White
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinBadge(coins = state.coins)
                IconButton(onClick = onPause) {
                    Icon(Icons.Filled.Pause, "Pause", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun HudStat(label: String, value: String, valueColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/** A single card with a flip animation around the Y axis (spec §8). */
@Composable
private fun MemoryCard(card: Card, cardBack: String, enabled: Boolean, onClick: () -> Unit) {
    val faceUp = card.isFaceUp || card.isMatched
    val rotation by animateFloatAsState(
        targetValue = if (faceUp) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "flip"
    )
    val showingFront = rotation > 90f

    Box(
        modifier = Modifier
            .aspectRatio(0.78f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (showingFront) {
                    if (card.isMatched) FortuneGoldLight else Color.White
                } else Color(0xFF1B1B1B) // dark chip tray behind the poker-chip back
            )
            // Gold trim gives each card a premium casino-chip feel.
            .border(
                BorderStroke(if (card.isMatched) 3.dp else 1.5.dp, GoldTrim),
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled && !faceUp, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (showingFront) {
            // Counter-rotate so the glyph isn't mirrored after the flip.
            Text(
                card.symbol.glyph,
                fontSize = 34.sp,
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            )
        } else {
            PokerChipBack(cardBack)
        }
    }
}

fun formatTime(totalSeconds: Int): String {
    val s = abs(totalSeconds)
    return "%d:%02d".format(s / 60, s % 60)
}
