package com.rabbitsluckandfortuneppamobs.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rabbitsluckandfortuneppamobs.models.Difficulty
import com.rabbitsluckandfortuneppamobs.models.GameMode
import com.rabbitsluckandfortuneppamobs.models.Level
import com.rabbitsluckandfortuneppamobs.ui.components.FortuneBackground
import com.rabbitsluckandfortuneppamobs.ui.components.ScreenTitle
import com.rabbitsluckandfortuneppamobs.ui.components.StarRow
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneGold
import com.rabbitsluckandfortuneppamobs.ui.theme.FortuneRed
import com.rabbitsluckandfortuneppamobs.ui.theme.InkBrown

/** Level selection grid with unlock state and earned stars (spec §5.3). */
@Composable
fun LevelSelectScreen(
    levels: List<Level>,
    starsFor: (Int) -> Int,
    selectedBackground: String,
    onSelect: (Level, GameMode) -> Unit,
    onBack: () -> Unit
) {
    var timedMode by remember { mutableStateOf(false) }

    FortuneBackground(selectedBackground = selectedBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                ScreenTitle("Select Level", modifier = Modifier.padding(start = 4.dp))
            }

            // Timed Mode toggle (spec §4.3): when on, levels start against a countdown.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "⏱️  Timed Mode",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Switch(
                    checked = timedMode,
                    onCheckedChange = { timedMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FortuneGold
                    )
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(levels, key = { it.levelId }) { level ->
                    LevelCell(
                        level = level,
                        stars = starsFor(level.levelId),
                        onClick = {
                            if (level.isUnlocked) {
                                onSelect(level, if (timedMode) GameMode.TIMED else GameMode.CLASSIC)
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun difficultyColor(difficulty: Difficulty): Color = when (difficulty) {
    Difficulty.EASY -> Color(0xFF66BB6A)
    Difficulty.MEDIUM -> Color(0xFF42A5F5)
    Difficulty.HARD -> Color(0xFFEF6C00)
    Difficulty.EXPERT -> Color(0xFFAB47BC)
}

@Composable
private fun LevelCell(level: Level, stars: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = level.isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (level.isUnlocked) Color.White else Color(0xFFBDA9A0),
        shadowElevation = 3.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (!level.isUnlocked) {
                Icon(Icons.Filled.Lock, "Locked", tint = InkBrown.copy(alpha = 0.6f))
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${level.levelId}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = FortuneRed
                    )
                    StarRow(stars = stars, starSize = 11)
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(difficultyColor(level.difficulty))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            level.difficulty.displayName,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
